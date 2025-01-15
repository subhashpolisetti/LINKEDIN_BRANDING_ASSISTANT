package com.linkedinjobassistant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedinjobassistant.model.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final SqsClient sqsClient;
    private final RedisTemplate<String, String> jobsRedisTemplate;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    private static final int MAX_MESSAGES = 10;
    private static final Duration MESSAGE_WAIT_TIME = Duration.ofSeconds(5);
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    /**
     * Fetch jobs from SQS and cache them
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void fetchAndCacheJobs() {
        log.info("Starting job fetch from SQS");
        
        try {
            List<Job> jobs = fetchJobsFromSQS();
            if (!jobs.isEmpty()) {
                cacheJobs(jobs);
                log.info("Successfully cached {} jobs", jobs.size());
            }
        } catch (Exception e) {
            log.error("Error fetching jobs from SQS", e);
        }
    }

    /**
     * Get cached jobs
     */
    public List<Job> getJobs() {
        String currentHourKey = getCurrentHourKey();
        String cachedJobs = jobsRedisTemplate.opsForValue().get(currentHourKey);
        
        if (cachedJobs != null) {
            try {
                return objectMapper.readValue(cachedJobs, new TypeReference<List<Job>>() {});
            } catch (JsonProcessingException e) {
                log.error("Error deserializing cached jobs", e);
            }
        }
        
        return new ArrayList<>();
    }

    /**
     * Fetch jobs from SQS
     */
    private List<Job> fetchJobsFromSQS() {
        List<Job> jobs = new ArrayList<>();
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(MAX_MESSAGES)
                .waitTimeSeconds((int) MESSAGE_WAIT_TIME.toSeconds())
                .build();

        List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();
        
        for (Message message : messages) {
            try {
                Map<String, Object> messageBody = objectMapper.readValue(message.body(), new TypeReference<Map<String, Object>>() {});
                
                // Check message timestamp
                String timestamp = (String) messageBody.get("timestamp");
                if (timestamp != null) {
                    Instant messageTime = Instant.parse(timestamp);
                    if (messageTime.isAfter(oneHourAgo)) {
                        // Process jobs from message
                        List<Job> messageJobs = parseJobs(messageBody);
                        jobs.addAll(messageJobs);
                        
                        // Delete processed message
                        deleteMessage(message);
                    }
                }
            } catch (Exception e) {
                log.error("Error processing SQS message", e);
            }
        }

        return jobs;
    }

    /**
     * Parse jobs from message body
     */
    private List<Job> parseJobs(Map<String, Object> messageBody) throws JsonProcessingException {
        Object jobsObj = messageBody.get("jobs");
        if (jobsObj != null) {
            String jobsJson = objectMapper.writeValueAsString(jobsObj);
            return objectMapper.readValue(jobsJson, new TypeReference<List<Job>>() {});
        }
        return new ArrayList<>();
    }

    /**
     * Delete processed message from SQS
     */
    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();
        
        sqsClient.deleteMessage(deleteRequest);
    }

    /**
     * Cache jobs in Redis
     */
    private void cacheJobs(List<Job> jobs) throws JsonProcessingException {
        String currentHourKey = getCurrentHourKey();
        String jobsJson = objectMapper.writeValueAsString(jobs);
        
        jobsRedisTemplate.opsForValue().set(currentHourKey, jobsJson, CACHE_TTL);
    }

    /**
     * Generate Redis key for current hour
     */
    private String getCurrentHourKey() {
        return String.format("jobs:%s", Instant.now().truncatedTo(ChronoUnit.HOURS));
    }

    /**
     * Get job by ID from cache
     */
    public Optional<Job> getJobById(String jobId) {
        return getJobs().stream()
                .filter(job -> job.getId().equals(jobId))
                .findFirst();
    }
}
