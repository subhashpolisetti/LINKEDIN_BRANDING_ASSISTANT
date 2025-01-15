package com.linkedinjobassistant.controller;

import com.linkedinjobassistant.model.Profile;
import com.linkedinjobassistant.service.AIService;
import com.linkedinjobassistant.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final AIService aiService;
    private final ResumeService resumeService;

    /**
     * Generate LinkedIn profile from resume
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateProfile(
            @RequestParam("resume") MultipartFile file,
            @AuthenticationPrincipal OidcUser user) {
        try {
            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Please upload a PDF file"));
            }

            // Process resume first
            String resumeText = resumeService.extractText(file);
            
            // Generate profile
            Profile profile = aiService.generateProfile(resumeText);
            
            // Calculate profile strength
            profile.updateProfileStrength();
            
            // Add default profile picture if not present
            if (profile.getProfilePicture() == null || profile.getProfilePicture().isEmpty()) {
                profile.setProfilePicture("https://via.placeholder.com/150");
            }
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error generating profile", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to generate profile: " + e.getMessage()));
        }
    }

    /**
     * Generate profile from existing resume
     */
    @PostMapping("/generate/{resumeId}")
    public ResponseEntity<?> generateProfileFromExisting(
            @PathVariable String resumeId,
            @AuthenticationPrincipal OidcUser user) {
        try {
            return resumeService.getResume(resumeId, user.getSubject())
                    .map(resume -> {
                        Profile profile = aiService.generateProfile(resume.getText());
                        profile.updateProfileStrength();
                        
                        if (profile.getProfilePicture() == null || profile.getProfilePicture().isEmpty()) {
                            profile.setProfilePicture("https://via.placeholder.com/150");
                        }
                        
                        return ResponseEntity.ok(profile);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error generating profile from existing resume", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to generate profile: " + e.getMessage()));
        }
    }

    /**
     * Update profile sections
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(
            @RequestBody Profile profile,
            @AuthenticationPrincipal OidcUser user) {
        try {
            // Validate profile data
            if (profile.getName() == null || profile.getName().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Name is required"));
            }

            // Update profile strength
            profile.updateProfileStrength();
            
            return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "profile", profile,
                "strength", profile.getProfileStrength()
            ));
        } catch (Exception e) {
            log.error("Error updating profile", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to update profile: " + e.getMessage()));
        }
    }

    /**
     * Get profile strength analysis
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeProfile(
            @RequestBody Profile profile,
            @AuthenticationPrincipal OidcUser user) {
        try {
            profile.updateProfileStrength();
            
            // Calculate section completeness
            Map<String, Object> analysis = Map.of(
                "overall_strength", profile.getProfileStrength(),
                "sections", Map.of(
                    "basic_info", calculateBasicInfoScore(profile),
                    "experience", calculateExperienceScore(profile),
                    "education", calculateEducationScore(profile),
                    "skills", calculateSkillsScore(profile),
                    "additional", calculateAdditionalScore(profile)
                ),
                "recommendations", generateRecommendations(profile)
            );
            
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Error analyzing profile", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to analyze profile: " + e.getMessage()));
        }
    }

    // Helper methods for profile analysis
    private int calculateBasicInfoScore(Profile profile) {
        int score = 0;
        if (profile.getName() != null && !profile.getName().isEmpty()) score += 25;
        if (profile.getHeadline() != null && !profile.getHeadline().isEmpty()) score += 25;
        if (profile.getAbout() != null && !profile.getAbout().isEmpty()) score += 25;
        if (profile.getLocation() != null && !profile.getLocation().isEmpty()) score += 25;
        return score;
    }

    private int calculateExperienceScore(Profile profile) {
        if (profile.getExperience() == null || profile.getExperience().isEmpty()) return 0;
        return Math.min(100, profile.getExperience().size() * 20);
    }

    private int calculateEducationScore(Profile profile) {
        if (profile.getEducation() == null || profile.getEducation().isEmpty()) return 0;
        return Math.min(100, profile.getEducation().size() * 50);
    }

    private int calculateSkillsScore(Profile profile) {
        if (profile.getSkills() == null || profile.getSkills().isEmpty()) return 0;
        return Math.min(100, profile.getSkills().size() * 10);
    }

    private int calculateAdditionalScore(Profile profile) {
        int score = 0;
        if (profile.getProjects() != null && !profile.getProjects().isEmpty()) score += 30;
        if (profile.getCertifications() != null && !profile.getCertifications().isEmpty()) score += 30;
        if (profile.getAwards() != null && !profile.getAwards().isEmpty()) score += 20;
        if (profile.getRecommendations() != null && !profile.getRecommendations().isEmpty()) score += 20;
        return score;
    }

    private Map<String, String> generateRecommendations(Profile profile) {
        Map<String, String> recommendations = new java.util.HashMap<>();
        
        if (profile.getAbout() == null || profile.getAbout().isEmpty()) {
            recommendations.put("about", "Add a professional summary to showcase your expertise");
        }
        
        if (profile.getExperience() == null || profile.getExperience().isEmpty()) {
            recommendations.put("experience", "Add your work experience to highlight your career progression");
        }
        
        if (profile.getSkills() == null || profile.getSkills().size() < 5) {
            recommendations.put("skills", "Add more skills to showcase your technical expertise");
        }
        
        if (profile.getProjects() == null || profile.getProjects().isEmpty()) {
            recommendations.put("projects", "Add projects to demonstrate practical experience");
        }
        
        return recommendations;
    }
}
