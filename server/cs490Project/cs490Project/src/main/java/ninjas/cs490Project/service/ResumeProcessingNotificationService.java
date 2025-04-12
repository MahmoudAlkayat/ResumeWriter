package ninjas.cs490Project.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

@Service
public class ResumeProcessingNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(ResumeProcessingNotificationService.class);
    private final Map<Integer, SseEmitter> resumeEmitters = new ConcurrentHashMap<>();
    private final Map<Integer, SseEmitter> careerEmitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SseEmitter subscribeToResumeProcessing(int resumeId) {
        logger.info("Creating SSE emitter for resumeId: {}", resumeId);
        SseEmitter emitter = new SseEmitter(0L); // No timeout
        
        // Add completion handler
        emitter.onCompletion(() -> {
            logger.info("SSE completed for resumeId: {}", resumeId);
            resumeEmitters.remove(resumeId);
        });
        
        // Add error handler
        emitter.onError((ex) -> {
            logger.error("SSE error for resumeId: {} - {}", resumeId, ex.getMessage());
            resumeEmitters.remove(resumeId);
        });
        
        resumeEmitters.put(resumeId, emitter);
        return emitter;
    }

    public SseEmitter subscribeToCareerProcessing(int userId) {
        SseEmitter emitter = new SseEmitter(0L); // No timeout
        careerEmitters.put(userId, emitter);
        
        emitter.onCompletion(() -> {
            logger.info("SSE completed for userId: {}", userId);
            careerEmitters.remove(userId);
        });
        
        emitter.onError((ex) -> {
            logger.error("SSE error for userId: {} - {}", userId, ex.getMessage());
            careerEmitters.remove(userId);
        });
        
        return emitter;
    }

    public void notifyProcessingComplete(int resumeId) {
        logger.info("Attempting to notify completion for resumeId: {}", resumeId);
        SseEmitter emitter = resumeEmitters.get(resumeId);
        if (emitter != null) {
            try {
                String eventData = "{\"status\":\"completed\",\"resumeId\":" + resumeId + "}";
                logger.info("Sending completion event: {}", eventData);
                emitter.send(SseEmitter.event()
                    .name("processing-complete")
                    .data(eventData));
                emitter.complete();
                logger.info("Successfully sent completion event for resumeId: {}", resumeId);
            } catch (Exception e) {
                logger.error("Error sending SSE notification for resumeId: {} - {}", resumeId, e.getMessage());
                emitter.completeWithError(e);
            }
        } else {
            logger.warn("No emitter found for resumeId: {}", resumeId);
        }
    }

    public void notifyCareerProcessingComplete(int userId) {
        SseEmitter emitter = careerEmitters.get(userId);
        if (emitter != null) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("status", "complete");
                data.put("userId", userId);
                emitter.send(SseEmitter.event()
                        .name("processing-complete")
                        .data(objectMapper.writeValueAsString(data)));
                emitter.complete();
                careerEmitters.remove(userId);
            } catch (Exception e) {
                logger.error("Error sending career processing complete notification", e);
                emitter.completeWithError(e);
                careerEmitters.remove(userId);
            }
        }
    }

    public void notifyCareerProcessingError(int userId, String errorMessage) {
        SseEmitter emitter = careerEmitters.get(userId);
        if (emitter != null) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("status", "error");
                data.put("userId", userId);
                data.put("message", errorMessage);
                
                // Send the error event
                emitter.send(SseEmitter.event()
                        .name("processing-error")
                        .data(objectMapper.writeValueAsString(data)));
                
                // Complete the emitter
                emitter.complete();
                
                // Remove the emitter from the map
                careerEmitters.remove(userId);
                
                logger.info("Sent career processing error notification for user {}: {}", userId, errorMessage);
            } catch (Exception e) {
                logger.error("Error sending career processing error notification", e);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    logger.error("Error completing emitter with error", ex);
                }
                careerEmitters.remove(userId);
            }
        } else {
            logger.warn("No emitter found for user {} when trying to send error notification", userId);
        }
    }
}