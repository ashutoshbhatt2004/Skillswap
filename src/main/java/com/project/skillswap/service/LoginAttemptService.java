package com.project.skillswap.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MILLIS = 10L * 60L * 1000L;
    private static final long LOCKOUT_MILLIS = 15L * 60L * 1000L;

    private final Map<String, ConcurrentLinkedDeque<Long>> failedAttemptsByKey = new ConcurrentHashMap<>();
    private final Map<String, Long> lockoutUntilByKey = new ConcurrentHashMap<>();

    public boolean isLocked(String email, String clientIp) {
        cleanupExpiredEntries(System.currentTimeMillis());
        String normalizedEmail = normalizeKey(email);
        String normalizedIp = normalizeKey(clientIp);
        return isKeyLocked(normalizedEmail) || isKeyLocked(normalizedIp);
    }

    public void recordFailedAttempt(String email, String clientIp) {
        long now = System.currentTimeMillis();
        cleanupExpiredEntries(now);

        recordFailureForKey(normalizeKey(email), now);
        recordFailureForKey(normalizeKey(clientIp), now);
    }

    public void clearAttempts(String email, String clientIp) {
        failedAttemptsByKey.remove(normalizeKey(email));
        failedAttemptsByKey.remove(normalizeKey(clientIp));
        lockoutUntilByKey.remove(normalizeKey(email));
        lockoutUntilByKey.remove(normalizeKey(clientIp));
    }

    private void recordFailureForKey(String key, long now) {
        if (key == null || key.isBlank()) {
            return;
        }

        ConcurrentLinkedDeque<Long> attempts = failedAttemptsByKey.computeIfAbsent(key, ignored -> new ConcurrentLinkedDeque<>());

        while (!attempts.isEmpty() && now - attempts.peekFirst() > WINDOW_MILLIS) {
            attempts.pollFirst();
        }

        attempts.addLast(now);
        while (attempts.size() > MAX_ATTEMPTS) {
            attempts.pollFirst();
        }

        if (attempts.size() >= MAX_ATTEMPTS) {
            lockoutUntilByKey.put(key, now + LOCKOUT_MILLIS);
        }
    }

    private boolean isKeyLocked(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }

        Long expiresAt = lockoutUntilByKey.get(key);
        if (expiresAt == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (expiresAt <= now) {
            lockoutUntilByKey.remove(key);
            failedAttemptsByKey.remove(key);
            return false;
        }

        return true;
    }

    private void cleanupExpiredEntries(long now) {
        failedAttemptsByKey.entrySet().removeIf(entry -> {
            ConcurrentLinkedDeque<Long> attempts = entry.getValue();
            while (!attempts.isEmpty() && now - attempts.peekFirst() > WINDOW_MILLIS) {
                attempts.pollFirst();
            }
            if (attempts.isEmpty()) {
                lockoutUntilByKey.remove(entry.getKey());
                return true;
            }
            return false;
        });

        lockoutUntilByKey.entrySet().removeIf(entry -> {
            if (entry.getValue() <= now) {
                failedAttemptsByKey.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    private String normalizeKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }
}
