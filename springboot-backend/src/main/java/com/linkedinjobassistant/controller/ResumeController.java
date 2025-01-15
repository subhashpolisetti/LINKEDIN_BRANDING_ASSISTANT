package com.linkedinjobassistant.controller;

import com.linkedinjobassistant.model.Resume;
import com.linkedinjobassistant.service.AIService;
import com.linkedinjobassistant.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeService resumeService;
    private final AIService aiService;

    /**
     * Upload a new resume
     */
    @PostMapping("/resume")
    public ResponseEntity<?> uploadResume(
            @RequestParam("resume") MultipartFile file,
            @AuthenticationPrincipal OidcUser user) {
        try {
            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please upload a PDF file"));
            }

            Resume resume = resumeService.processResume(file, user.getSubject());
            
            return ResponseEntity.ok(Map.of(
                "resume_id", resume.getId(),
                "message", "Resume uploaded successfully"
            ));
        } catch (Exception e) {
            log.error("Error uploading resume", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to process resume: " + e.getMessage()));
        }
    }

    /**
     * Analyze resume against job description
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeJob(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal OidcUser user) {
        try {
            String resumeId = request.get("resume_id");
            String jobDescription = request.get("job_description");

            if (resumeId == null || jobDescription == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing resume_id or job_description"));
            }

            return resumeService.getResume(resumeId, user.getSubject())
                    .map(resume -> {
                        Map<String, Object> analysis = aiService.analyzeJobMatch(
                                resume.getText(), jobDescription);
                        resumeService.updateAnalysis(resumeId, user.getSubject(), 
                                analysis.toString());
                        return ResponseEntity.ok(analysis);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error analyzing job", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Analysis failed: " + e.getMessage()));
        }
    }

    /**
     * Generate tailored resume points
     */
    @PostMapping("/tailor")
    public ResponseEntity<?> tailorResume(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal OidcUser user) {
        try {
            String resumeId = (String) request.get("resume_id");
            String jobDescription = (String) request.get("job_description");
            @SuppressWarnings("unchecked")
            List<String> keywords = (List<String>) request.get("keywords");

            if (resumeId == null || jobDescription == null || keywords == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing required parameters"));
            }

            return resumeService.getResume(resumeId, user.getSubject())
                    .map(resume -> {
                        List<String> points = aiService.generateTailoredPoints(
                                resume.getText(), jobDescription, keywords);
                        return ResponseEntity.ok(Map.of("tailored_points", points));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error generating tailored points", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to generate points: " + e.getMessage()));
        }
    }

    /**
     * Delete a resume
     */
    @DeleteMapping("/resume/{id}")
    public ResponseEntity<?> deleteResume(
            @PathVariable String id,
            @AuthenticationPrincipal OidcUser user) {
        try {
            resumeService.deleteResume(id, user.getSubject());
            return ResponseEntity.ok(Map.of("message", "Resume deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting resume", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to delete resume: " + e.getMessage()));
        }
    }

    /**
     * Get user's resumes
     */
    @GetMapping("/resumes")
    public ResponseEntity<?> getUserResumes(@AuthenticationPrincipal OidcUser user) {
        try {
            List<Resume> resumes = resumeService.findByUserId(user.getSubject());
            return ResponseEntity.ok(resumes);
        } catch (Exception e) {
            log.error("Error fetching user resumes", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch resumes: " + e.getMessage()));
        }
    }
}
