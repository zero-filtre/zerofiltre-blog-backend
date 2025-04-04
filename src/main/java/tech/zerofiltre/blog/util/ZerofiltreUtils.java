package tech.zerofiltre.blog.util;

import lombok.extern.slf4j.Slf4j;
import tech.zerofiltre.blog.domain.Product;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.security.config.EmailValidator;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class ZerofiltreUtils {

    public static final String ROOT_URL = "https://zerofiltre.tech";

    private ZerofiltreUtils() {
    }

    public static boolean isMentored(Product product) {
        return product instanceof Course && ((Course) product).isMentored();
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            log.error("Error when trying to sleep", e);
        }
    }

    public static String getValidEmail(User user) {
        boolean validEmail = user.getEmail() != null && EmailValidator.validateEmail(user.getEmail());
        return validEmail ? user.getEmail() : user.getPaymentEmail();
    }

    public static String getOriginUrl(String env) {
        return env.equals("prod") ? ROOT_URL : "https://" + env + ".zerofiltre.tech";
    }

    public static String hex(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
        }
        return sb.toString();
    }

    public static String md5Hex(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return hex(md.digest(message.getBytes("CP1252")));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            log.error("We couldn't generate the hex to get the gravatar image", e);
        }
        return null;
    }

    public static String toHumanReadable(long timestamp) {
        LocalDateTime dateTime = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        return dateTime.format(formatter);
    }

    public static List<LocalDateTime> getBeginningAndEndOfMonthDates() {
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), 1, 0, 0);
        LocalDateTime startDate = endDate.minusMonths(1);

        return Arrays.asList(startDate, endDate);
    }

    public static <T> Collection<List<T>> partitionList(List<T> list, int n) {
        return IntStream.range(0, list.size()).boxed()
                .collect(Collectors.groupingBy(i -> i / n,
                        Collectors.mapping(list::get, Collectors.toList())))
                .values();
    }

    public static String sanitizeString(String filename) {
        return filename.replaceAll("[/\\\\:*?\"<>| ]+", "_");
    }

    public static boolean isValidEmail(String email) {
        return EmailValidator.validateEmail(email);
    }

}
