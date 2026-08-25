package com.project.skillswap.repository;

import com.project.skillswap.entity.Review;
import com.project.skillswap.entity.Request;
import com.project.skillswap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Optional<Review> findByRequest(Request request);
    List<Review> findByRequest_Skill_Mentor(User mentor);
}
