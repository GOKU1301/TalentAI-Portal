package com.soprasteria.smartjobportal.repository;

import com.soprasteria.smartjobportal.model.Application;
import com.soprasteria.smartjobportal.model.Job;
import com.soprasteria.smartjobportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Integer> {
    List<Application> findByUser(User user);
    List<Application> findByJob(Job job);
    Optional<Application> findByJobAndUser(Job job, User user);
    boolean existsByJobAndUser(Job job, User user);
}
