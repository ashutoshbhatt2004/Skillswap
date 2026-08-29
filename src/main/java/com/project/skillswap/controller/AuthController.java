package com.project.skillswap.controller;

import com.project.skillswap.entity.User;
import com.project.skillswap.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // --- REGISTRATION LOGIC ---
    @PostMapping("/register")
    public String registerUser(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            @RequestParam(value = "role", required = false) String role) {

        // Trim inputs
        String nameTrim = fullName == null ? null : fullName.trim();
        String emailTrim = email == null ? null : email.trim().toLowerCase();
        String roleTrim = User.normalizeRole(role);

        // 1) Full name validation
        if (nameTrim == null || nameTrim.isBlank()) {
            return "redirect:/register?error=Name%20is%20required.";
        }

        // 2) Email validation
        if (emailTrim == null || emailTrim.isBlank()) {
            return "redirect:/register?error=Please%20enter%20an%20email.";
        }
        // Simple email regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!emailTrim.matches(emailRegex)) {
            return "redirect:/register?error=Please%20enter%20a%20valid%20email.";
        }
        // Check duplicate
        if (userService.emailExists(emailTrim)) {
            return "redirect:/register?error=Email%20already%20exists.";
        }

        // 3) Password validation
        if (password == null || password.isBlank()) {
            return "redirect:/register?error=Password%20is%20required.";
        }
        if (password.length() < 6) {
            return "redirect:/register?error=Password%20must%20be%20at%20least%206%20characters.";
        }

        // 4) Confirm password validation
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            return "redirect:/register?error=Passwords%20do%20not%20match.";
        }

        // 5) Role validation (only STUDENT or MENTOR allowed)
        if (roleTrim == null || (!"STUDENT".equals(roleTrim) && !"MENTOR".equals(roleTrim))) {
            return "redirect:/register?error=Invalid%20role.";
        }

        // Passed validation — create user and save (UserService will handle BCrypt hashing)
        User newUser = new User();
        newUser.setFullName(nameTrim);
        newUser.setEmail(emailTrim);
        newUser.setPassword(password); // do not log or modify; UserService.saveUser will hash
        newUser.setRole(roleTrim);

        userService.saveUser(newUser);

        return "redirect:/login?success=Account%20created.";
    }

    @PostMapping("/login")
    public String loginUser(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session,
            HttpServletRequest request) {

        System.out.println("--- LOGIN ATTEMPT --- Email: [" + email + "]");

        User authenticatedUser = userService.authenticate(email, password);

        if (authenticatedUser != null) {
            System.out.println("Login SUCCESS! Role hai: " + authenticatedUser.getRole());

            if (session == null && request != null) {
                session = request.getSession(true);
            }

            // Protect against session fixation: rotate the session id after successful authentication.
            try {
                if (request != null) {
                    request.changeSessionId();
                }
            } catch (NoSuchMethodError | UnsupportedOperationException ex) {
                try {
                    if (session != null) {
                        java.util.Map<String, Object> attrs = new java.util.HashMap<>();
                        java.util.Enumeration<String> names = session.getAttributeNames();
                        while (names.hasMoreElements()) {
                            String name = names.nextElement();
                            attrs.put(name, session.getAttribute(name));
                        }
                        session.invalidate();
                        HttpSession newSession = request != null ? request.getSession(true) : null;
                        if (newSession != null) {
                            for (java.util.Map.Entry<String, Object> e : attrs.entrySet()) {
                                newSession.setAttribute(e.getKey(), e.getValue());
                            }
                            session = newSession;
                        }
                    }
                } catch (IllegalStateException ignore) {
                    // If session invalidation fails, continue and set attribute on current session
                }
            }

            if (session != null) {
                session.setAttribute("loggedInUser", authenticatedUser);
            }

            if ("MENTOR".equals(authenticatedUser.getRole())) {
                return "redirect:/mentor-dashboard";
            }
            if ("ADMIN".equals(authenticatedUser.getRole())) {
                return "redirect:/admin-dashboard";
            }
            return "redirect:/student-dashboard";
        } else {
            System.out.println("Login FAILED! Email ya password database se match nahi hua.");
            return "redirect:/login?error=true";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            try {
                session.invalidate();
            } catch (IllegalStateException ex) {
                // session already invalidated - ignore
            }
        }
        return "redirect:/login";
    }
}