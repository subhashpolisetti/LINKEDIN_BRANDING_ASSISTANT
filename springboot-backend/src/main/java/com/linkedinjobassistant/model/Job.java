package com.linkedinjobassistant.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    
    private String id;
    private String title;
    private String companyName;
    private String jobPostingUrl;
    private String description;
    private String location;
    private List<String> skills;
    private String employmentType;
    private String experienceLevel;
    private Instant listedAt;
    private String source;  // e.g., "LinkedIn", "Indeed", etc.
    
    // Additional fields for job analysis
    private Double matchScore;
    private List<String> matchedKeywords;
    private List<String> missingKeywords;
    
    // Helper method to calculate time since posting
    public long getHoursSincePosting() {
        if (listedAt == null) {
            return 0;
        }
        Instant now = Instant.now();
        return java.time.Duration.between(listedAt, now).toHours();
    }
    
    // Helper method to check if job is recent (within last 24 hours)
    public boolean isRecent() {
        return getHoursSincePosting() <= 24;
    }
    
    // Helper method to update match analysis
    public void updateMatchAnalysis(Double score, List<String> matched, List<String> missing) {
        this.matchScore = score;
        this.matchedKeywords = matched;
        this.missingKeywords = missing;
    }
    
    // Method to create a cache key for Redis
    public String createCacheKey() {
        return String.format("job:%s", id);
    }
}
