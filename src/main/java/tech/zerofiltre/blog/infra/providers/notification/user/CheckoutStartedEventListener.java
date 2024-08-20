package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.notification.user.model.CheckoutStartedEvent;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;
import tech.zerofiltre.blog.infra.security.config.EmailValidator;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.*;

@Slf4j
@Component
public class CheckoutStartedEventListener implements ApplicationListener<CheckoutStartedEvent> {

    private final MessageSource messages;
    private final ZerofiltreEmailSender emailSender;
    private final ITemplateEngine emailTemplateEngine;
    private final List<CheckoutStartedEvent> events = new ArrayList<>();
    private final long checkoutReminderDelayMs;
    private final long checkoutReminderCheckFrequencyMs;

    public CheckoutStartedEventListener(MessageSource messages, ZerofiltreEmailSender emailSender, ITemplateEngine emailTemplateEngine,InfraProperties infraProperties) {
        this.messages = messages;
        this.emailSender = emailSender;
        this.emailTemplateEngine = emailTemplateEngine;
        checkoutReminderDelayMs = infraProperties.getCheckoutReminderDelayMs();
        checkoutReminderCheckFrequencyMs = infraProperties.getCheckoutReminderCheckFrequencyMs();
        new Thread(() -> {
            for (; ; ) {
                handleEventIfNeeded();
                try {
                    Thread.sleep(checkoutReminderCheckFrequencyMs);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }, "checkout-reminder").start();
    }

    void handleEventIfNeeded() {
        Iterator<CheckoutStartedEvent> iterator = events.iterator();
        while (iterator.hasNext()) {
            CheckoutStartedEvent event = iterator.next();
            long now = System.currentTimeMillis();
            if (now - event.getTimestamp() >= checkoutReminderDelayMs) {
                log.debug("Event {} was received on {}, current time {}, reminding checkout", event, ZerofiltreUtils.toHumanReadable(event.getTimestamp()), ZerofiltreUtils.toHumanReadable(now));
                handleEvent(event);
                iterator.remove();
            } else {
                log.debug("Event {} was received on {}, current time {}, do not remind checkout yet", event, ZerofiltreUtils.toHumanReadable(event.getTimestamp()), ZerofiltreUtils.toHumanReadable(now));
            }
        }
    }

    @Override
    public void onApplicationEvent(CheckoutStartedEvent event) {
        events.add(event);
    }

    void handleEvent(CheckoutStartedEvent event) {
        User user = event.getUser();

        boolean validEmail = user.getEmail() != null && EmailValidator.validateEmail(user.getEmail());
        String emailAddress = validEmail ? user.getEmail() : user.getPaymentEmail();
        if (emailAddress != null) {


            String subjectCode = "message.checkout.reminder.subject";

            String pageUri = "/cours";

            String template = "checkout_reminder.html";

            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("fullName", user.getFullName());
            templateModel.put("backToCheckoutLink", event.getAppUrl() + pageUri);

            String recipientAddress = user.getEmail();
            String subject = messages.getMessage(subjectCode, null, event.getLocale());
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);
            thymeleafContext.setLocale(event.getLocale());
            String emailContent = emailTemplateEngine.process(template, thymeleafContext);

            Email email = new Email();
            email.setSubject(subject);
            email.setContent(emailContent);
            email.setRecipients(Collections.singletonList(recipientAddress));
            emailSender.send(email, true);
        }
    }

    public List<CheckoutStartedEvent> getEvents() {
        return events;
    }
}
