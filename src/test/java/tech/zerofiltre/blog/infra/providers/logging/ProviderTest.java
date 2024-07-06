package tech.zerofiltre.blog.infra.providers.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class ProviderTest {
    @Test
    void testingSomething() {
        assertEquals(1, 1);
    }
}