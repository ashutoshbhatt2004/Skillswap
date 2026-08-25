package com.project.skillswap.controller;

import com.project.skillswap.entity.Skill;
import com.project.skillswap.entity.User;
import com.project.skillswap.repository.SkillRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SkillController {

    @Autowired
    private SkillRepository skillRepository;

    // --- 1. BROWSE SKILLS PAGE (Saari skills dikhana) ---
    @GetMapping("/browse-skills")
    public String browseSkills(Model model) {
        List<Skill> skillList = skillRepository.findAll(); // Database se saari skills nikal rahe hain
        model.addAttribute("skills", skillList);
        return "browse-skills"; // browse-skills.html ko data bhej rahe hain
    }

    // --- ADD SKILL PAGE DIKHANE KE LIYE (GET) ---
    @GetMapping("/add-skill")
    public String showAddSkillPage(HttpSession session) {
        // Sirf check kar rahe hain ki user login hai ya nahi
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof User)) {
            return "redirect:/login";
        }
        User user = (User) obj;
        if (!"MENTOR".equals(user.getRole())) {
            if ("ADMIN".equals(user.getRole())) return "redirect:/admin-dashboard";
            return "redirect:/student-dashboard";
        }
        return "add-skill"; // Yeh aapki add-skill.html file ko screen par dikhayega
    }

    // --- 2. ADD SKILL LOGIC (Mentor द्वारा skill add karna) ---
    @PostMapping("/add-skill")
    public String addSkill(
            @RequestParam("name") String name,
            @RequestParam("category") String category,
            @RequestParam("experienceLevel") String experienceLevel,
            HttpSession session) {

        // Check karenge ki kya user logged-in hai
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof User)) {
            return "redirect:/login";
        }
        User loggedInUser = (User) obj;
        if (!"MENTOR".equals(loggedInUser.getRole())) {
            if ("ADMIN".equals(loggedInUser.getRole())) return "redirect:/admin-dashboard";
            return "redirect:/student-dashboard";
        }

        // Simple server-side validation
        if (name == null || name.trim().isEmpty() || category == null || category.trim().isEmpty() || experienceLevel == null || experienceLevel.trim().isEmpty()) {
            return "redirect:/add-skill?error=Please+fill+all+required+fields";
        }

        // Nayi skill object banakar data set kar rahe hain
        Skill skill = new Skill();
        skill.setName(name.trim());
        skill.setCategory(category.trim());
        skill.setExperienceLevel(experienceLevel.trim());
        skill.setMentor(loggedInUser); // Mentor comes from session only

        skillRepository.save(skill); // Database mein save

        return "redirect:/mentor-dashboard"; // Save hone ke baad mentor dashboard par wapas
    }
}