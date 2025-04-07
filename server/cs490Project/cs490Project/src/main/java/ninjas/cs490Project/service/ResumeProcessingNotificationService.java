package ninjas.cs490Project.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResumeProcessingNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(ResumeProcessingNotificationService.class);
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(int resumeId) {
        logger.info("Creating SSE emitter for resumeId: {}", resumeId);
        SseEmitter emitter = new SseEmitter(30_000L); // 30 seconds timeout
        
        // Add timeout handler
        emitter.onTimeout(() -> {
            logger.warn("SSE timeout for resumeId: {}", resumeId);
            emitters.remove(resumeId);
        });
        
        // Add completion handler
        emitter.onCompletion(() -> {
            logger.info("SSE completed for resumeId: {}", resumeId);
            emitters.remove(resumeId);
        });
        
        // Add error handler
        emitter.onError((ex) -> {
            logger.error("SSE error for resumeId: {} - {}", resumeId, ex.getMessage());
            emitters.remove(resumeId);
        });
        
        emitters.put(resumeId, emitter);
        return emitter;
    }

    public void notifyProcessingComplete(int resumeId) {
        logger.info("Attempting to notify completion for resumeId: {}", resumeId);
        SseEmitter emitter = emitters.get(resumeId);
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
                emitters.remove(resumeId);
            }
        } else {
            logger.warn("No emitter found for resumeId: {}", resumeId);
        }
    }
}