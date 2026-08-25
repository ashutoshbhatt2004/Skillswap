package com.project.skillswap.controller;

import com.project.skillswap.entity.Request;
import com.project.skillswap.entity.User;
import com.project.skillswap.service.RequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class RequestController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private com.project.skillswap.service.ReviewService reviewService;

    @PostMapping("/requests/create")
    public String createRequest(@RequestParam("skillId") Integer skillId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        if (!"STUDENT".equals(user.getRole())) return "redirect:/student-dashboard?error=auth";

        String result = requestService.createRequest(user, skillId);
        if ("SUCCESS".equals(result)) {
            return "redirect:/student-dashboard?success=request_sent";
        }
        return "redirect:/skill-details?id=" + skillId + "&error=" + result.replaceAll(" ", "%20");
    }

    @PostMapping("/requests/{id}/accept")
    public String accept(@PathVariable("id") Integer id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        if (!"MENTOR".equals(user.getRole())) return "redirect:/mentor-dashboard?error=auth";

        String res = requestService.acceptRequest(user, id);
        return "redirect:/mentor-dashboard" + ("SUCCESS".equals(res) ? "?success=accepted" : "?error=" + res);
    }

    @PostMapping("/requests/{id}/reject")
    public String reject(@PathVariable("id") Integer id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        if (!"MENTOR".equals(user.getRole())) return "redirect:/mentor-dashboard?error=auth";

        String res = requestService.rejectRequest(user, id);
        return "redirect:/mentor-dashboard" + ("SUCCESS".equals(res) ? "?success=rejected" : "?error=" + res);
    }

    @GetMapping("/requests/sent")
    public String sentRequests(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        if (!"STUDENT".equals(user.getRole())) return "redirect:/student-dashboard?error=auth";

        List<Request> list = requestService.getSentRequests(user);
        // Determine which requests already have reviews
        java.util.Set<Integer> reviewedIds = new java.util.HashSet<>();
        for (Request r : list) {
            reviewService.findByRequest(r).ifPresent(rv -> reviewedIds.add(r.getId()));
        }
        model.addAttribute("sentRequests", list);
        model.addAttribute("reviewedIds", reviewedIds);
        return "requests-sent";
    }

    @GetMapping("/reviews/create/{requestId}")
    public String showReviewForm(@PathVariable("requestId") Integer requestId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        if (!"STUDENT".equals(user.getRole())) return "redirect:/student-dashboard?error=auth";

        java.util.Optional<Request> maybe = requestService.findById(requestId);
        if (maybe.isEmpty()) return "redirect:/requests/sent?error=request_not_found";
        Request req = maybe.get();

        if (req.getStudent() == null || !user.getId().equals(req.getStudent().getId())) {
            return "redirect:/requests/sent?error=not_owner";
        }
        if (!"ACCEPTED".equals(req.getStatus())) {
            return "redirect:/requests/sent?error=not_accepted";
        }
        if (reviewService.findByRequest(req).isPresent()) {
            return "redirect:/requests/sent?error=already_reviewed";
        }

        model.addAttribute("request", req);
        return "review-form";
    }

    @PostMapping("/reviews/create/{requestId}")
    public String submitReview(@PathVariable("requestId") Integer requestId,
                               @RequestParam("rating") Integer rating,
                               @RequestParam(value = "comment", required = false) String comment,
                               HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        if (!"STUDENT".equals(user.getRole())) return "redirect:/student-dashboard?error=auth";

        java.util.Optional<Request> maybe = requestService.findById(requestId);
        if (maybe.isEmpty()) return "redirect:/requests/sent?error=request_not_found";
        Request req = maybe.get();

        if (req.getStudent() == null || !user.getId().equals(req.getStudent().getId())) {
            return "redirect:/requests/sent?error=not_owner";
        }
        if (!"ACCEPTED".equals(req.getStatus())) {
            return "redirect:/requests/sent?error=not_accepted";
        }
        if (reviewService.findByRequest(req).isPresent()) {
            return "redirect:/requests/sent?error=already_reviewed";
        }

        if (rating == null || rating < 1 || rating > 5) {
            return "redirect:/reviews/create/" + requestId + "?error=invalid_rating";
        }

        String res = reviewService.createReview(req, rating, comment);
        if ("SUCCESS".equals(res)) {
            return "redirect:/requests/sent?success=review_created";
        }
        return "redirect:/reviews/create/" + requestId + "?error=" + res.replaceAll(" ", "%20");
    }

    @GetMapping("/requests/received")
    public String receivedRequests(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        if (!"MENTOR".equals(user.getRole())) return "redirect:/mentor-dashboard?error=auth";

        List<Request> list = requestService.getReceivedRequests(user);
        model.addAttribute("receivedRequests", list);
        return "mentor-dashboard";
    }
}
