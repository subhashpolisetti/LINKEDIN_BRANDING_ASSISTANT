package com.linkedinjobassistant.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "resumes")
public class Resume {
    
    @Id
    private String id;
    
    private String text;
    
    private String filename;
    
    @CreatedDate
    private Instant createdAt;
    
    private String userId;
    
    // Cached analysis results
    private String cachedAnalysis;
    
    // Constructor for new resume
    public Resume(String text, String filename, String userId) {
        this.text = text;
        this.filename = filename;
        this.userId = userId;
        this.createdAt = Instant.now();
    }
    
    // Default constructor required by MongoDB
    public Resume() {
    }
    
    // Method to update cached analysis
    public void updateCachedAnalysis(String analysis) {
        this.cachedAnalysis = analysis;
    }
}
