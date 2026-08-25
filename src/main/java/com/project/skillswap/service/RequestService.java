package com.project.skillswap.service;

import com.project.skillswap.entity.Request;
import com.project.skillswap.entity.Skill;
import com.project.skillswap.entity.User;
import com.project.skillswap.repository.RequestRepository;
import com.project.skillswap.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private SkillRepository skillRepository;

    public Optional<Request> findById(Integer id) {
        return requestRepository.findById(id);
    }

    public List<Request> getSentRequests(User student) {
        return requestRepository.findByStudent(student);
    }

    public List<Request> getReceivedRequests(User mentor) {
        return requestRepository.findBySkill_Mentor(mentor);
    }

    public String createRequest(User student, Integer skillId) {
        Optional<Skill> maybeSkill = skillRepository.findById(skillId);
        if (maybeSkill.isEmpty()) return "Skill not found";

        Skill skill = maybeSkill.get();

        if (skill.getMentor() != null && student.getId().equals(skill.getMentor().getId())) {
            return "Cannot request your own skill";
        }

        boolean exists = requestRepository.existsByStudentAndSkillAndStatus(student, skill, "PENDING");
        if (exists) return "You already have a pending request for this skill";

        Request r = new Request();
        r.setStudent(student);
        r.setSkill(skill);
        r.setStatus("PENDING");
        requestRepository.save(r);
        return "SUCCESS";
    }

    public String acceptRequest(User mentor, Integer requestId) {
        Optional<Request> maybe = requestRepository.findById(requestId);
        if (maybe.isEmpty()) return "Request not found";
        Request r = maybe.get();
        if (!"PENDING".equals(r.getStatus())) return "Request already processed";
        if (r.getSkill() == null || r.getSkill().getMentor() == null || !mentor.getId().equals(r.getSkill().getMentor().getId())) {
            return "Not authorized";
        }
        r.setStatus("ACCEPTED");
        requestRepository.save(r);
        return "SUCCESS";
    }

    public String rejectRequest(User mentor, Integer requestId) {
        Optional<Request> maybe = requestRepository.findById(requestId);
        if (maybe.isEmpty()) return "Request not found";
        Request r = maybe.get();
        if (!"PENDING".equals(r.getStatus())) return "Request already processed";
        if (r.getSkill() == null || r.getSkill().getMentor() == null || !mentor.getId().equals(r.getSkill().getMentor().getId())) {
            return "Not authorized";
        }
        r.setStatus("REJECTED");
        requestRepository.save(r);
        return "SUCCESS";
    }
}
