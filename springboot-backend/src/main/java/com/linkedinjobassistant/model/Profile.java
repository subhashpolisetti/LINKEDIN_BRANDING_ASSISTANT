package com.linkedinjobassistant.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    
    private String name;
    private String headline;
    private String location;
    private String about;
    private String profilePicture;
    private Integer profileStrength;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Experience {
        private String title;
        private String company;
        private String duration;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Education {
        private String degree;
        private String school;
        private String duration;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Project {
        private String name;
        private String description;
        private String technologies;
        private String duration;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Certification {
        private String name;
        private String issuer;
        private String date;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Award {
        private String name;
        private String issuer;
        private String date;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        private String text;
        private String recommender;
    }
    
    private List<Experience> experience;
    private List<Education> education;
    private List<Project> projects;
    private List<Certification> certifications;
    private List<String> skills;
    private List<Award> awards;
    private List<Recommendation> recommendations;
    
    // Helper method to calculate profile completeness
    public int calculateProfileStrength() {
        int strength = 0;
        
        // Basic information
        if (name != null && !name.isEmpty()) strength += 10;
        if (headline != null && !headline.isEmpty()) strength += 10;
        if (about != null && !about.isEmpty()) strength += 15;
        if (location != null && !location.isEmpty()) strength += 5;
        
        // Experience
        if (experience != null && !experience.isEmpty()) strength += 20;
        
        // Education
        if (education != null && !education.isEmpty()) strength += 15;
        
        // Skills
        if (skills != null && !skills.isEmpty()) strength += 10;
        
        // Additional sections
        if (projects != null && !projects.isEmpty()) strength += 5;
        if (certifications != null && !certifications.isEmpty()) strength += 5;
        if (awards != null && !awards.isEmpty()) strength += 5;
        
        return Math.min(strength, 100);
    }
    
    // Method to update profile strength
    public void updateProfileStrength() {
        this.profileStrength = calculateProfileStrength();
    }
}
