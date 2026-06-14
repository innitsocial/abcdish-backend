package com.innitsocial.abcdish.stories.controller;

import com.innitsocial.abcdish.common.security.SecurityUtils;
import com.innitsocial.abcdish.stories.dto.StoryRequest;
import com.innitsocial.abcdish.stories.dto.StoryResponse;
import com.innitsocial.abcdish.stories.dto.StoryViewerResponse;
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

    @DeleteMapping("/{id}")
    public void deleteStory(@PathVariable Long id) {
        storyService.deleteStory(SecurityUtils.currentUserId(), id);
    }

    @PostMapping("/{id}/views")
    public StoryResponse recordView(@PathVariable Long id) {
        return storyService.recordView(SecurityUtils.currentUserId(), id);
    }

    @PostMapping("/{id}/likes")
    public StoryResponse likeStory(@PathVariable Long id) {
        return storyService.likeStory(SecurityUtils.currentUserId(), id);
    }

    @DeleteMapping("/{id}/likes")
    public StoryResponse unlikeStory(@PathVariable Long id) {
        return storyService.unlikeStory(SecurityUtils.currentUserId(), id);
    }

    @GetMapping("/{id}/viewers")
    public List<StoryViewerResponse> getViewers(@PathVariable Long id) {
        return storyService.getViewers(SecurityUtils.currentUserId(), id);
    }
}
