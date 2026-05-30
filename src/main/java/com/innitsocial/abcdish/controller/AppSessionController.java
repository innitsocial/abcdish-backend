package com.innitsocial.abcdish.controller;

import com.innitsocial.abcdish.config.SecurityUtils;
import com.innitsocial.abcdish.dto.AppSessionResponse;
import com.innitsocial.abcdish.service.AppSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppSessionController {

    private final AppSessionService appSessionService;

    @GetMapping("/session")
    public AppSessionResponse getSession() {
        return appSessionService.getSession(SecurityUtils.currentUserId());
    }
}