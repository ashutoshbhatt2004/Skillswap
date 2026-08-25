package com.project.skillswap.service;

import com.project.skillswap.entity.User;
import com.project.skillswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public User saveUser(User user) {
        // If the password is provided and is not already a BCrypt hash, encode it.
        if (user.getPassword() != null) {
            String pw = user.getPassword();
            if (!isBCryptHash(pw)) {
                user.setPassword(passwordEncoder.encode(pw));
            }
        }
        return userRepository.save(user);
    }

    public boolean emailExists(String email) {
        if (email == null) return false;
        return userRepository.findByEmail(email).isPresent();
    }

    private boolean isBCryptHash(String s) {
        if (s == null) return false;
        return s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$");
    }

    // Authenticate supports existing plaintext passwords by upgrading them to BCrypt on successful login.
    public User authenticate(String email, String password) {
        Optional<User> maybe = userRepository.findByEmail(email);
        if (maybe.isEmpty()) return null;
        User user = maybe.get();
        String stored = user.getPassword();
        if (stored == null) return null;

        if (isBCryptHash(stored)) {
            // Verify with BCrypt
            if (passwordEncoder.matches(password, stored)) {
                return user;
            }
            return null;
        } else {
            // Stored password appears plaintext — verify directly, then upgrade
            if (stored.equals(password)) {
                // Upgrade: hash password and save user
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                return user;
            }
            return null;
        }
    }
}