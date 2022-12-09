package tech.zerofiltre.blog.infra.security.config;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {

    @Test
    void testValidEmails() {
        assertTrue(EmailValidator.validateEmail("user@domain.com"));
        assertTrue(EmailValidator.validateEmail("user.name@domain.com"));
        assertTrue(EmailValidator.validateEmail("user+name@domain.com"));
        assertTrue(EmailValidator.validateEmail("user+58@domain.com"));
    }

    @Test
    void testInvalidEmails() {
        assertFalse(EmailValidator.validateEmail("user@domain"));
        assertFalse(EmailValidator.validateEmail("user@domain."));
        assertFalse(EmailValidator.validateEmail("user@.com"));
        assertFalse(EmailValidator.validateEmail("@domain.com"));
        assertFalse(EmailValidator.validateEmail("user@domain@domain.com"));
    }

}