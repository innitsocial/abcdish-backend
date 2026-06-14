package com.innitsocial.abcdish.common.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innitsocial.abcdish.content.service.MealTranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.jobs.worker", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BackgroundJobWorker {

    private final BackgroundJobService backgroundJobService;
    private final MealTranslationService mealTranslationService;
    private final ObjectMapper objectMapper;

    @Value("${app.jobs.worker.batch-size:5}")
    private int batchSize;

    @Value("${app.jobs.worker.max-attempts:5}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${app.jobs.worker.fixed-delay-ms:5000}")
    public void processJobs() {
        int safeBatchSize = Math.max(1, Math.min(batchSize, 25));
        for (int index = 0; index < safeBatchSize; index++) {
            var claimed = backgroundJobService.claimNextJob(maxAttempts);
            if (claimed.isEmpty()) {
                return;
            }
            processJob(claimed.get());
        }
    }

    private void processJob(BackgroundJobService.ClaimedJob job) {
        try {
            switch (job.type()) {
                case "TRANSLATE_MEAL" -> processMealTranslation(job.payloadJson());
                default -> log.warn("Unknown background job type={} id={}", job.type(), job.id());
            }
            backgroundJobService.complete(job.id());
        } catch (Exception error) {
            log.warn("Background job failed id={} type={} attempt={}: {}",
                    job.id(), job.type(), job.attempts(), error.getMessage());
            backgroundJobService.fail(job.id(), error.getMessage(), job.attempts(), maxAttempts);
        }
    }

    private void processMealTranslation(String payloadJson) throws Exception {
        JsonNode payload = objectMapper.readTree(payloadJson);
        long mealId = payload.path("mealId").asLong();
        String languageCode = payload.path("languageCode").asText();
        mealTranslationService.generateQueuedTranslation(mealId, languageCode);
    }
}
