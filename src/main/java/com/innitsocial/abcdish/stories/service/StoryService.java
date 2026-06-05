package com.innitsocial.abcdish.stories.service;

import com.innitsocial.abcdish.auth.entity.AppUser;
import com.innitsocial.abcdish.auth.repository.AppUserRepository;
import com.innitsocial.abcdish.stories.dto.StoryRequest;
import com.innitsocial.abcdish.stories.dto.StoryResponse;
import com.innitsocial.abcdish.stories.entity.Story;
import com.innitsocial.abcdish.stories.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StoryService {

    private final StoryRepository storyRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public List<StoryResponse> getActiveStories() {
        return storyRepository.findTop30ByExpiresAtAfterOrderByCreatedAtDesc(LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StoryResponse createStory(Long userId, StoryRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Story story = storyRepository.save(Story.builder()
                .userId(userId)
                .title(request.title().trim())
                .caption(clean(request.caption()))
                .imageUrl(clean(request.imageUrl()))
                .videoUrl(clean(request.videoUrl()))
                .build());

        return StoryResponse.fromEntity(story, user);
    }

    private StoryResponse toResponse(Story story) {
        AppUser user = appUserRepository.findById(story.getUserId())
                .orElseThrow(() -> new RuntimeException("Story user not found"));

        return StoryResponse.fromEntity(story, user);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
