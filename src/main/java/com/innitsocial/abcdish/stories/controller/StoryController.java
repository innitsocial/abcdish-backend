package com.innitsocial.abcdish.stories.controller;

import com.innitsocial.abcdish.common.security.SecurityUtils;
import com.innitsocial.abcdish.stories.dto.StoryRequest;
import com.innitsocial.abcdish.stories.dto.StoryResponse;
import com.innitsocial.abcdish.stories.service.StoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @GetMapping
    public List<StoryResponse> getStories() {
        return storyService.getActiveStories();
    }

    @PostMapping
    public StoryResponse createStory(@Valid @RequestBody StoryRequest request) {
        return storyService.createStory(SecurityUtils.currentUserId(), request);
    }
}
