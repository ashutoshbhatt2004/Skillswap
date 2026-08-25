package com.project.skillswap.repository;

import com.project.skillswap.entity.Request;
import com.project.skillswap.entity.Skill;
import com.project.skillswap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {
	List<Request> findByStudent(User student);

	List<Request> findBySkill_Mentor(User mentor);

	boolean existsByStudentAndSkillAndStatus(User student, Skill skill, String status);
}