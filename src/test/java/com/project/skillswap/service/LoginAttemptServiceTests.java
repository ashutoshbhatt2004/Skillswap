package com.project.skillswap.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTests {

    @Test
    void shouldLockAccountAfterFiveFailuresWithinTenMinutes() {
        LoginAttemptService service = new LoginAttemptService();
        String email = "student@example.com";
        String ip = "127.0.0.1";

        for (int i = 0; i < 4; i++) {
            service.recordFailedAttempt(email, ip);
            assertFalse(service.isLocked(email, ip));
        }

        service.recordFailedAttempt(email, ip);
        assertTrue(service.isLocked(email, ip));
    }

    @Test
    void shouldClearCountersAfterSuccessfulLogin() {
        LoginAttemptService service = new LoginAttemptService();
        String email = "mentor@example.com";
        String ip = "192.168.1.1";

        for (int i = 0; i < 5; i++) {
            service.recordFailedAttempt(email, ip);
        }

        assertTrue(service.isLocked(email, ip));

        service.clearAttempts(email, ip);

        assertFalse(service.isLocked(email, ip));
    }
}
