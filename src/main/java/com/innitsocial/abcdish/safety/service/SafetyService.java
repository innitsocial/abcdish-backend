package com.innitsocial.abcdish.safety.service;

import com.innitsocial.abcdish.safety.dto.ContentReportRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SafetyService {

    public void reportContent(Long reporterUserId, ContentReportRequest request) {
        log.warn(
                "CONTENT_REPORT reporterUserId={} targetType={} targetId={} reason={} details={}",
                reporterUserId,
                clean(request.targetType()),
                clean(request.targetId()),
                clean(request.reason()),
                clean(request.details())
        );
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
