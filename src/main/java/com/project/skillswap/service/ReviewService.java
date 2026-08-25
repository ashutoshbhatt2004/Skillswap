package com.project.skillswap.service;

import com.project.skillswap.entity.Review;
import com.project.skillswap.entity.Request;
import com.project.skillswap.repository.ReviewRepository;
import com.project.skillswap.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RequestRepository requestRepository;

    public Optional<Review> findByRequest(Request request) {
        return reviewRepository.findByRequest(request);
    }

    // Create review only after validations. Returns "SUCCESS" or error message.
    public String createReview(Request request, Integer rating, String comment) {
        if (request == null) return "Request not found";

        // Ensure the latest request status is loaded from DB if necessary
        Optional<Request> maybe = requestRepository.findById(request.getId());
        if (maybe.isEmpty()) return "Request not found";
        Request r = maybe.get();

        if (!"ACCEPTED".equals(r.getStatus())) return "Request not accepted";

        Optional<Review> existing = reviewRepository.findByRequest(r);
        if (existing.isPresent()) return "Review already exists for this request";

        if (rating == null || rating < 1 || rating > 5) return "Invalid rating";

        Review review = new Review();
        review.setRequest(r);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);
        return "SUCCESS";
    }
}
