package com.innitsocial.abcdish.controller;

import com.innitsocial.abcdish.config.SecurityUtils;
import com.innitsocial.abcdish.dto.VideoAccessResponse;
import com.innitsocial.abcdish.service.VideoViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video-views")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
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