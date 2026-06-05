package com.innitsocial.abcdish.social.controller;

import com.innitsocial.abcdish.common.security.SecurityUtils;
import com.innitsocial.abcdish.social.dto.CommentRequest;
import com.innitsocial.abcdish.social.dto.CommentResponse;
import com.innitsocial.abcdish.social.dto.SocialActionResponse;
import com.innitsocial.abcdish.social.service.SocialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;

    @PostMapping("/meals/{mealId}/likes")
    public SocialActionResponse likeMeal(@PathVariable Long mealId) {
        return socialService.likeMeal(mealId, SecurityUtils.currentUserId());
    }

    @DeleteMapping("/meals/{mealId}/likes")
    public SocialActionResponse unlikeMeal(@PathVariable Long mealId) {
        return socialService.unlikeMeal(mealId, SecurityUtils.currentUserId());
    }

    @PostMapping("/meals/{mealId}/shares")
    public SocialActionResponse shareMeal(@PathVariable Long mealId) {
        return socialService.recordShare(mealId, SecurityUtils.currentUserId());
    }

    @GetMapping("/meals/{mealId}/comments")
    public List<CommentResponse> getComments(@PathVariable Long mealId) {
        return socialService.getMealComments(mealId);
    }

    @PostMapping("/meals/{mealId}/comments")
    public CommentResponse addComment(
            @PathVariable Long mealId,
            @Valid @RequestBody CommentRequest request
    ) {
        return socialService.addComment(mealId, SecurityUtils.currentUserId(), request);
    }

    @PostMapping("/creators/{creatorKey}/follow")
    public SocialActionResponse followCreator(@PathVariable String creatorKey) {
        return socialService.followCreator(creatorKey, SecurityUtils.currentUserId());
    }

    @DeleteMapping("/creators/{creatorKey}/follow")
    public SocialActionResponse unfollowCreator(@PathVariable String creatorKey) {
        return socialService.unfollowCreator(creatorKey, SecurityUtils.currentUserId());
    }
}
