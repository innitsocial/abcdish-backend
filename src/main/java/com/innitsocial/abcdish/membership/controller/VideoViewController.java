package com.innitsocial.abcdish.membership.controller;

import com.innitsocial.abcdish.common.security.SecurityUtils;
import com.innitsocial.abcdish.membership.dto.VideoAccessResponse;
import com.innitsocial.abcdish.membership.service.VideoViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video-views")
@RequiredArgsConstructor
public class VideoViewController {

    private final VideoViewService videoViewService;

    @PostMapping("/{mealId}")
    public VideoAccessResponse recordView(@PathVariable Long mealId) {
        return videoViewService.recordView(
                SecurityUtils.currentUserId(),
                mealId
        );
    }

    @GetMapping("/usage")
    public VideoAccessResponse usage() {
        return videoViewService.usage(SecurityUtils.currentUserId());
    }
}