package tech.zerofiltre.blog.infra.security.config;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EmailListValidatorTest {

    @Test
    void testValidEmails() {
        assertTrue(EmailListValidator.validateEmail(Arrays.asList("user@domain.com", "user.name@domain.com", "user+name@domain.com", "user+58@domain.com")));
    }

    @Test
    void testInvalidEmails() {
        assertFalse(EmailListValidator.validateEmail(Arrays.asList("user@domain", "user@domain.", "user@.com", "@domain.com", "user@domain@domain.com")));
    }

}