package com.linkedinjobassistant.repository;

import com.linkedinjobassistant.model.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends MongoRepository<Resume, String> {
    
    // Find resumes by user ID
    List<Resume> findByUserId(String userId);
    
    // Find resumes by user ID and created after a specific date
    List<Resume> findByUserIdAndCreatedAtAfter(String userId, Instant date);
    
    // Find resume by ID and user ID (for security)
    Optional<Resume> findByIdAndUserId(String id, String userId);
    
    // Find resumes with cached analysis
    @Query("{ 'userId': ?0, 'cachedAnalysis': { $exists: true, $ne: null } }")
    List<Resume> findAnalyzedResumesByUserId(String userId);
    
    // Delete resumes by user ID
    void deleteByUserId(String userId);
    
    // Delete old resumes (for cleanup)
    void deleteByCreatedAtBefore(Instant date);
    
    // Count resumes by user ID
    long countByUserId(String userId);
    
    // Check if user has any resumes
    boolean existsByUserId(String userId);
}
