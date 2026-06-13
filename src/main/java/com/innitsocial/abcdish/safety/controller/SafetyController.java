package com.innitsocial.abcdish.safety.controller;

import com.innitsocial.abcdish.common.security.SecurityUtils;
import com.innitsocial.abcdish.safety.dto.ContentReportRequest;
import com.innitsocial.abcdish.safety.service.SafetyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/safety")
@RequiredArgsConstructor
public class SafetyController {

    private final SafetyService safetyService;

    @PostMapping("/reports")
    public void reportContent(@Valid @RequestBody ContentReportRequest request) {
        safetyService.reportContent(SecurityUtils.currentUserId(), request);
    }
}
