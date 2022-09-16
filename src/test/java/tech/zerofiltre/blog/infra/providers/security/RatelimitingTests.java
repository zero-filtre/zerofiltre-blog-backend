//package tech.zerofiltre.blog.infra.providers.security;
//
//import org.junit.jupiter.api.*;
//import org.springframework.http.*;
//
//public class RatelimitingTests {
//
//    @Test
//    public void whenRequestNotExceedingCapacity_thenReturnOkResponse() {
//        ResponseEntity<String> response = restTemplate.getForEntity("", String.class);
//        assertEquals(OK, response.getStatusCode());
//
//        HttpHeaders headers = response.getHeaders();
//        String key = "rate-limit-application_serviceSimple_127.0.0.1";
//
//        assertEquals("5", headers.getFirst(HEADER_LIMIT + key));
//        assertEquals("4", headers.getFirst(HEADER_REMAINING + key));
//        assertThat(
//                parseInt(headers.getFirst(HEADER_RESET + key)),
//                is(both(greaterThanOrEqualTo(0)).and(lessThanOrEqualTo(60000)))
//        );
//    }
//}
