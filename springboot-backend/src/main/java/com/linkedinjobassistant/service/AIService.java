package com.linkedinjobassistant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedinjobassistant.model.Job;
import com.linkedinjobassistant.model.Profile;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final OpenAiService openAiService;
    private final String googleApiKey;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

    /**
     * Analyze resume against job description using Gemini AI
     */
    public Map<String, Object> analyzeJobMatch(String resumeText, String jobDescription) {
        String prompt = buildJobMatchPrompt(resumeText, jobDescription);
        
        try {
            String response = callGeminiAPI(prompt);
            return parseGeminiResponse(response);
        } catch (Exception e) {
            log.error("Error analyzing job match with Gemini AI", e);
            return createDefaultAnalysis();
        }
    }

    /**
     * Generate tailored resume points using Gemini AI
     */
    public List<String> generateTailoredPoints(String resumeText, String jobDescription, List<String> keywords) {
        String prompt = buildTailoredPointsPrompt(resumeText, jobDescription, keywords);
        
        try {
            String response = callGeminiAPI(prompt);
            return parseTailoredPoints(response);
        } catch (Exception e) {
            log.error("Error generating tailored points with Gemini AI", e);
            return createDefaultPoints();
        }
    }

    /**
     * Generate LinkedIn profile using OpenAI
     */
    public Profile generateProfile(String resumeText) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", "You are a professional resume analyzer that creates detailed LinkedIn profiles."));
        messages.add(new ChatMessage("user", buildProfilePrompt(resumeText)));

        try {
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .temperature(0.7)
                    .maxTokens(3000)
                    .build();

            String response = openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();

            return parseProfileResponse(response);
        } catch (Exception e) {
            log.error("Error generating profile with OpenAI", e);
            return createDefaultProfile();
        }
    }

    /**
     * Call Gemini AI API
     */
    private String callGeminiAPI(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        contents.put("text", prompt);
        requestBody.put("contents", List.of(contents));
        
        String url = GEMINI_API_URL + "?key=" + googleApiKey;
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(url, request, String.class);
    }

    /**
     * Parse Gemini response for job match analysis
     */
    private Map<String, Object> parseGeminiResponse(String response) throws JsonProcessingException {
        return objectMapper.readValue(response, Map.class);
    }

    /**
     * Parse Gemini response for tailored points
     */
    private List<String> parseTailoredPoints(String response) throws JsonProcessingException {
        Map<String, Object> parsed = objectMapper.readValue(response, Map.class);
        return (List<String>) parsed.get("tailored_points");
    }

    /**
     * Parse OpenAI response for profile generation
     */
    private Profile parseProfileResponse(String response) throws JsonProcessingException {
        return objectMapper.readValue(response, Profile.class);
    }

    /**
     * Build prompts
     */
    private String buildJobMatchPrompt(String resumeText, String jobDescription) {
        return String.format("""
            You are an experienced Applicant Tracking System (ATS) specializing in the tech industry.
            Analyze the provided job description and extract all important keywords.
            
            Required JSON format:
            {
                "JD Match": "70%%",
                "JD Keywords": ["Java", "Spring", "SQL", "Cloud"]
            }
            
            Resume text:
            %s
            
            Job Description:
            %s
            """, resumeText, jobDescription);
    }

    private String buildTailoredPointsPrompt(String resumeText, String jobDescription, List<String> keywords) {
        return String.format("""
            Generate 4 strong bullet points for experience or projects that incorporate the required keywords.
            Each bullet point should highlight relevant skills and achievements.
            
            Keywords: %s
            
            Resume:
            %s
            
            Job Description:
            %s
            """, String.join(", ", keywords), resumeText, jobDescription);
    }

    private String buildProfilePrompt(String resumeText) {
        return String.format("""
            Analyze the following resume and create a detailed LinkedIn-style profile.
            Extract ALL experiences, certifications, and other details from the resume.
            
            Resume content:
            %s
            """, resumeText);
    }

    /**
     * Default responses for error cases
     */
    private Map<String, Object> createDefaultAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("JD Match", "0%");
        analysis.put("JD Keywords", new ArrayList<>());
        return analysis;
    }

    private List<String> createDefaultPoints() {
        List<String> points = new ArrayList<>();
        points.add("Developed and implemented software solutions utilizing required programming languages and frameworks");
        points.add("Led technical projects and collaborated with cross-functional teams to deliver high-quality results");
        points.add("Optimized system performance and implemented best practices in software development");
        points.add("Contributed to the design and development of scalable applications and features");
        return points;
    }

    private Profile createDefaultProfile() {
        return Profile.builder()
                .name("Profile Generation Failed")
                .headline("Please try again later")
                .about("An error occurred while generating the profile.")
                .build();
    }
}
