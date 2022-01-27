package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.mail.*;
import org.springframework.mail.javamail.*;
import org.springframework.test.context.junit.jupiter.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class BlogEmailSenderTest {

    public static final String ADDRESS = "address";
    public static final String SUBJECT = "subject";
    public static final String CONTENT = "content";
    private BlogEmailSender blogEmailSender;

    @MockBean
    JavaMailSender javaMailSender;

    @BeforeEach
    void setUp() {
        blogEmailSender = new BlogEmailSender(javaMailSender);
    }

    @Test
    void mustSend_WithProperData() {
        //ARRANGE
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        //ACT
        blogEmailSender.send(ADDRESS, SUBJECT, CONTENT);

        //ASSERT
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(captor.capture());
        SimpleMailMessage mail = captor.getValue();
        assertThat(mail.getSubject()).isEqualTo(SUBJECT);
        assertThat(mail.getTo()).isNotNull();
        assertThat(Arrays.asList(mail.getTo()).contains(ADDRESS)).isTrue();
        assertThat(mail.getText()).isEqualTo(CONTENT);

    }
}