package com.soprasteria.smartjobportal.repository;

import com.soprasteria.smartjobportal.model.Job;
import com.soprasteria.smartjobportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Integer> {
    List<Job> findByPostedBy(User postedBy);
    
    @Query("SELECT j FROM Job j WHERE " +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.skills) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Job> searchJobs(@Param("keyword") String keyword);
    
    @Query(value = "SELECT j.* FROM jobs j WHERE " +
           "MATCH(j.skills) AGAINST(:skills IN BOOLEAN MODE)", 
           nativeQuery = true)
    List<Job> findMatchingJobs(@Param("skills") String skills);
}
