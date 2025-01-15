package com.linkedinjobassistant.controller;

import com.linkedinjobassistant.model.Job;
import com.linkedinjobassistant.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;

    /**
     * Get all available jobs
     */
    @GetMapping
    public ResponseEntity<?> getJobs(@AuthenticationPrincipal OidcUser user) {
        try {
            List<Job> jobs = jobService.getJobs();
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            log.error("Error fetching jobs", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch jobs: " + e.getMessage()));
        }
    }

    /**
     * Get job by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(
            @PathVariable String id,
            @AuthenticationPrincipal OidcUser user) {
        try {
            return jobService.getJobById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching job", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch job: " + e.getMessage()));
        }
    }

    /**
     * Manually trigger job refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshJobs(@AuthenticationPrincipal OidcUser user) {
        try {
            jobService.fetchAndCacheJobs();
            List<Job> jobs = jobService.getJobs();
            return ResponseEntity.ok(Map.of(
                "message", "Jobs refreshed successfully",
                "jobs", jobs
            ));
        } catch (Exception e) {
            log.error("Error refreshing jobs", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to refresh jobs: " + e.getMessage()));
        }
    }

    /**
     * Get job statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getJobStats(@AuthenticationPrincipal OidcUser user) {
        try {
            List<Job> jobs = jobService.getJobs();
            
            // Calculate statistics
            long totalJobs = jobs.size();
            long recentJobs = jobs.stream()
                    .filter(Job::isRecent)
                    .count();
            
            Map<String, Object> stats = Map.of(
                "total_jobs", totalJobs,
                "recent_jobs", recentJobs,
                "last_update", jobs.stream()
                    .map(Job::getListedAt)
                    .max(java.time.Instant::compareTo)
                    .orElse(null)
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching job statistics", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch job statistics: " + e.getMessage()));
        }
    }

    /**
     * Search jobs by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchJobs(
            @RequestParam String keyword,
            @AuthenticationPrincipal OidcUser user) {
        try {
            List<Job> jobs = jobService.getJobs();
            
            List<Job> matchingJobs = jobs.stream()
                    .filter(job -> 
                        (job.getTitle() != null && 
                            job.getTitle().toLowerCase().contains(keyword.toLowerCase())) ||
                        (job.getDescription() != null && 
                            job.getDescription().toLowerCase().contains(keyword.toLowerCase())) ||
                        (job.getSkills() != null && 
                            job.getSkills().stream()
                                .anyMatch(skill -> 
                                    skill.toLowerCase().contains(keyword.toLowerCase())))
                    )
                    .toList();
            
            return ResponseEntity.ok(matchingJobs);
        } catch (Exception e) {
            log.error("Error searching jobs", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to search jobs: " + e.getMessage()));
        }
    }

    /**
     * Filter jobs by criteria
     */
    @PostMapping("/filter")
    public ResponseEntity<?> filterJobs(
            @RequestBody Map<String, Object> filters,
            @AuthenticationPrincipal OidcUser user) {
        try {
            List<Job> jobs = jobService.getJobs();
            
            // Apply filters
            List<Job> filteredJobs = jobs.stream()
                    .filter(job -> {
                        boolean matches = true;
                        
                        if (filters.containsKey("location")) {
                            String location = (String) filters.get("location");
                            matches &= job.getLocation() != null && 
                                job.getLocation().toLowerCase().contains(location.toLowerCase());
                        }
                        
                        if (filters.containsKey("employmentType")) {
                            String type = (String) filters.get("employmentType");
                            matches &= job.getEmploymentType() != null && 
                                job.getEmploymentType().equalsIgnoreCase(type);
                        }
                        
                        if (filters.containsKey("experienceLevel")) {
                            String level = (String) filters.get("experienceLevel");
                            matches &= job.getExperienceLevel() != null && 
                                job.getExperienceLevel().equalsIgnoreCase(level);
                        }
                        
                        return matches;
                    })
                    .toList();
            
            return ResponseEntity.ok(filteredJobs);
        } catch (Exception e) {
            log.error("Error filtering jobs", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to filter jobs: " + e.getMessage()));
        }
    }
}
