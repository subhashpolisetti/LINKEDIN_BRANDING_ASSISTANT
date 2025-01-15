package com.linkedinjobassistant.service;

import com.linkedinjobassistant.model.Resume;
import com.linkedinjobassistant.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    // Cache TTL for resume text
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    
    // Pattern for cleaning text
    private static final Pattern CLEAN_PATTERN = Pattern.compile("([a-z])([A-Z])|([A-Z])([A-Z][a-z])|\\.|,|\\|");

    /**
     * Process and store a new resume
     */
    public Resume processResume(MultipartFile file, String userId) throws IOException {
        // Extract text from PDF
        String text = extractText(file);
        
        // Clean and normalize the text
        String cleanedText = cleanText(text);
        
        // Create and save resume
        Resume resume = new Resume(cleanedText, file.getOriginalFilename(), userId);
        resume = resumeRepository.save(resume);
        
        // Cache the resume text
        cacheResumeText(resume.getId(), cleanedText);
        
        return resume;
    }

    /**
     * Get resume by ID with caching
     */
    public Optional<Resume> getResume(String id, String userId) {
        // Try to get from cache first
        String cachedText = redisTemplate.opsForValue().get(getCacheKey(id));
        
        if (cachedText != null) {
            return resumeRepository.findByIdAndUserId(id, userId)
                .map(resume -> {
                    resume.setText(cachedText);
                    return resume;
                });
        }
        
        // If not in cache, get from database and cache it
        return resumeRepository.findByIdAndUserId(id, userId)
            .map(resume -> {
                cacheResumeText(id, resume.getText());
                return resume;
            });
    }

    /**
     * Extract text from PDF file
     */
    private String extractText(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Clean and normalize text
     */
    private String cleanText(String text) {
        if (text == null) return "";
        
        // Replace patterns with spaces
        String cleaned = CLEAN_PATTERN.matcher(text)
            .replaceAll(match -> {
                String matched = match.group();
                if (matched.length() == 2) {
                    return matched.charAt(0) + " " + matched.charAt(1);
                }
                return matched + " ";
            });
        
        // Normalize whitespace
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        
        return cleaned;
    }

    /**
     * Cache resume text in Redis
     */
    private void cacheResumeText(String resumeId, String text) {
        String key = getCacheKey(resumeId);
        redisTemplate.opsForValue().set(key, text, CACHE_TTL);
    }

    /**
     * Generate Redis cache key
     */
    private String getCacheKey(String resumeId) {
        return String.format("resume:%s", resumeId);
    }

    /**
     * Delete resume and clear cache
     */
    public void deleteResume(String id, String userId) {
        resumeRepository.findByIdAndUserId(id, userId).ifPresent(resume -> {
            resumeRepository.delete(resume);
            redisTemplate.delete(getCacheKey(id));
        });
    }

    /**
     * Update cached analysis
     */
    public void updateAnalysis(String id, String userId, String analysis) {
        resumeRepository.findByIdAndUserId(id, userId).ifPresent(resume -> {
            resume.updateCachedAnalysis(analysis);
            resumeRepository.save(resume);
        });
    }
}
