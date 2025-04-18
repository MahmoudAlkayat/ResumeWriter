package ninjas.cs490Project.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResumeProcessingNotificationService {
    private final Map<Integer, SseEmitter> resumeEmitters = new ConcurrentHashMap<>();
    private final Map<Integer, SseEmitter> careerEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribeToResumeProcessing(Integer resumeId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5-minute timeout
        resumeEmitters.put(resumeId, emitter);

        emitter.onCompletion(() -> resumeEmitters.remove(resumeId));
        emitter.onTimeout(() -> resumeEmitters.remove(resumeId));

        return emitter;
    }

    public SseEmitter subscribeToCareerProcessing(Integer freeformId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5-minute timeout
        careerEmitters.put(freeformId, emitter);

        emitter.onCompletion(() -> careerEmitters.remove(freeformId));
        emitter.onTimeout(() -> careerEmitters.remove(freeformId));

        return emitter;
    }

    public void notifyProcessingComplete(Integer resumeId) {
        SseEmitter emitter = resumeEmitters.get(resumeId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("resume-processing")
                        .data(Map.of(
                            "status", "complete",
                            "resumeId", resumeId
                        )));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                resumeEmitters.remove(resumeId);
            }
        }
    }

    public void notifyCareerProcessingComplete(Integer freeformId) {
        SseEmitter emitter = careerEmitters.get(freeformId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("career-processing")
                        .data(Map.of(
                            "status", "complete",
                            "freeformId", freeformId
                        )));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                careerEmitters.remove(freeformId);
            }
        }
    }

    public void notifyProcessingError(Integer resumeId, String errorMessage) {
        SseEmitter emitter = resumeEmitters.get(resumeId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("resume-processing")
                        .data(Map.of(
                            "status", "error",
                            "resumeId", resumeId,
                            "error", errorMessage
                        )));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                resumeEmitters.remove(resumeId);
            }
        }
    }

    public void notifyCareerProcessingError(Integer freeformId, String errorMessage) {
        SseEmitter emitter = careerEmitters.get(freeformId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("career-processing")
                        .data(Map.of(
                            "status", "error",
                            "freeformId", freeformId,
                            "error", errorMessage
                        )));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                careerEmitters.remove(freeformId);
            }
        }
    }
}