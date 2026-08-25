package com.project.skillswap.controller;

import com.project.skillswap.entity.Skill;
import com.project.skillswap.entity.User;
import com.project.skillswap.service.RequestService;
import com.project.skillswap.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private RequestService requestService;

    @Autowired
    private com.project.skillswap.repository.UserRepository userRepository;
    @Autowired
    private com.project.skillswap.service.UserService userService;
    @Autowired
    private com.project.skillswap.repository.ReviewRepository reviewRepository;

    @GetMapping({"/", "/index", "/index.html"})
    public String homePage() {
        return "index"; // Yeh templates/index.html ko load karega
    }

    @GetMapping({"/about", "/about.html"})
    public String aboutPage() {
        return "about";
    }

    @GetMapping({"/contact", "/contact.html"})
    public String contactPage() {
        return "contact";
    }

    @GetMapping({"/login", "/login.html"})
    public String loginPage() {
        return "login";
    }

    @GetMapping({"/register", "/register.html"})
    public String registerPage() {
        return "register";
    }

    // NOTE: browse-skills mapping removed from this controller to avoid duplicate
    // mapping. The SkillController provides the /browse-skills route and loads
    // the required skills into the model.

    @GetMapping({"/skill-details", "/skill-details.html"})
    public String skillDetailsPage(@RequestParam(value = "id", required = false) Integer id, Model model) {
        if (id != null) {
            Skill skill = skillRepository.findById(id).orElse(null);
            model.addAttribute("skill", skill);
        }
        return "skill-details";
    }

    

    @GetMapping({"/student-dashboard", "/student-dashboard.html"})
    public String studentDashboard(HttpSession session, Model model) {
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof User)) {
            return "redirect:/login";
        }
        User user = (User) obj;
        if (!"STUDENT".equals(user.getRole())) {
            return "redirect:/mentor-dashboard";
        }
        model.addAttribute("sentRequests", requestService.getSentRequests(user));
        return "student-dashboard";
    }

    @GetMapping({"/mentor-dashboard", "/mentor-dashboard.html"})
    public String mentorDashboard(HttpSession session, Model model) {
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof User)) {
            return "redirect:/login";
        }
        User user = (User) obj;
        if (!"MENTOR".equals(user.getRole())) {
            return "redirect:/student-dashboard";
        }
        model.addAttribute("receivedRequests", requestService.getReceivedRequests(user));
        return "mentor-dashboard";
    }


    @GetMapping({"/admin-dashboard", "/admin-dashboard.html"})
    public String adminDashboard(HttpSession session) {
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof User)) {
            return "redirect:/login";
        }
        User user = (User) obj;
        if (!"ADMIN".equals(user.getRole())) {
            if ("MENTOR".equals(user.getRole())) return "redirect:/mentor-dashboard";
            return "redirect:/student-dashboard";
        }
        return "admin-dashboard";
    }

    @GetMapping({"/admin/users", "/admin/users.html"})
    public String adminUsers(HttpSession session, Model model) {
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof User)) {
            return "redirect:/login";
        }
        User user = (User) obj;
        if (!"ADMIN".equals(user.getRole())) {
            if ("MENTOR".equals(user.getRole())) return "redirect:/mentor-dashboard";
            return "redirect:/student-dashboard";
        }
        java.util.List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping({"/admin/add-admin", "/admin/add-admin.html"})
    public String addAdminPage(HttpSession session, Model model) {
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof User)) {
            return "redirect:/login";
        }
        User user = (User) obj;
        if (!"ADMIN".equals(user.getRole())) {
            if ("MENTOR".equals(user.getRole())) return "redirect:/mentor-dashboard";
            return "redirect:/student-dashboard";
        }
        return "add-admin";
    }

    @PostMapping("/admin/add-admin")
    public String submitAddAdmin(@RequestParam("fullName") String fullName,
                                 @RequestParam("email") String email,
                                 @RequestParam("password") String password,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session) {
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof User)) {
            return "redirect:/login";
        }
        User user = (User) obj;
        if (!"ADMIN".equals(user.getRole())) {
            if ("MENTOR".equals(user.getRole())) return "redirect:/mentor-dashboard";
            return "redirect:/student-dashboard";
        }

        // Basic validation
        if (fullName == null || fullName.trim().isEmpty()) {
            return "redirect:/admin/add-admin?error=full_name_required";
        }
        if (email == null || email.trim().isEmpty()) {
            return "redirect:/admin/add-admin?error=email_required";
        }
        if (password == null || password.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
            return "redirect:/admin/add-admin?error=password_required";
        }
        if (!password.equals(confirmPassword)) {
            return "redirect:/admin/add-admin?error=password_mismatch";
        }

        // Check for existing email
        if (userRepository.findByEmail(email.trim()).isPresent()) {
            return "redirect:/admin/add-admin?error=email_taken";
        }

        com.project.skillswap.entity.User newUser = new com.project.skillswap.entity.User();
        newUser.setFullName(fullName.trim());
        newUser.setEmail(email.trim());
        newUser.setPassword(password); // keep existing plaintext mechanism for now
        newUser.setRole("ADMIN");

        userService.saveUser(newUser);

        return "redirect:/admin-dashboard?success=admin_created";
    }

    @GetMapping({"/admin/mentors", "/admin/mentors.html"})
    public String adminMentors(HttpSession session, Model model) {
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof User)) {
            return "redirect:/login";
        }
        User user = (User) obj;
        if (!"ADMIN".equals(user.getRole())) {
            if ("MENTOR".equals(user.getRole())) return "redirect:/mentor-dashboard";
            return "redirect:/student-dashboard";
        }
        java.util.List<User> mentors = userRepository.findByRole("MENTOR");
        model.addAttribute("mentors", mentors);
        return "mentors";
    }

    @GetMapping({"/mentor/reviews", "/mentor/reviews.html"})
    public String mentorReviews(HttpSession session, Model model) {
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof User)) {
            return "redirect:/login";
        }
        User user = (User) obj;
        if (!"MENTOR".equals(user.getRole())) {
            if ("ADMIN".equals(user.getRole())) return "redirect:/admin-dashboard";
            return "redirect:/student-dashboard";
        }
        java.util.List<com.project.skillswap.entity.Review> reviews = reviewRepository.findByRequest_Skill_Mentor(user);
        model.addAttribute("reviews", reviews);
        return "mentor-reviews";
    }

    @GetMapping({"/admin/skills", "/admin/skills.html"})
    public String adminSkills(HttpSession session, Model model) {
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof User)) {
            return "redirect:/login";
        }
        User user = (User) obj;
        if (!"ADMIN".equals(user.getRole())) {
            if ("MENTOR".equals(user.getRole())) return "redirect:/mentor-dashboard";
            return "redirect:/student-dashboard";
        }
        java.util.List<Skill> skills = skillRepository.findAll();
        model.addAttribute("skills", skills);
        return "skills";
    }

    @GetMapping({"/admin/reviews", "/admin/reviews.html"})
    public String adminReviews(HttpSession session, Model model) {
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof User)) {
            return "redirect:/login";
        }
        User user = (User) obj;
        if (!"ADMIN".equals(user.getRole())) {
            if ("MENTOR".equals(user.getRole())) return "redirect:/mentor-dashboard";
            return "redirect:/student-dashboard";
        }
        java.util.List<com.project.skillswap.entity.Review> reviews = reviewRepository.findAll();
        model.addAttribute("reviews", reviews);
        return "reviews";
    }

    @GetMapping({"/admin/settings", "/admin/settings.html"})
    public String adminSettings(HttpSession session, Model model) {
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof User)) {
            return "redirect:/login";
        }
        User user = (User) obj;
        if (!"ADMIN".equals(user.getRole())) {
            if ("MENTOR".equals(user.getRole())) return "redirect:/mentor-dashboard";
            return "redirect:/student-dashboard";
        }
        return "settings";
    }
}