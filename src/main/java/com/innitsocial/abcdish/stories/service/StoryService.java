package com.innitsocial.abcdish.stories.service;

import com.innitsocial.abcdish.auth.entity.AppUser;
import com.innitsocial.abcdish.auth.repository.AppUserRepository;
import com.innitsocial.abcdish.common.security.SecurityUtils;
import com.innitsocial.abcdish.contest.entity.ContestEntry;
import com.innitsocial.abcdish.contest.repository.ContestEntryRepository;
import com.innitsocial.abcdish.moderation.ContentModerationService;
import com.innitsocial.abcdish.moderation.ModerationResult;
import com.innitsocial.abcdish.moderation.ModerationStatus;
import com.innitsocial.abcdish.stories.dto.StoryRequest;
import com.innitsocial.abcdish.stories.dto.StoryResponse;
import com.innitsocial.abcdish.stories.dto.StoryViewerResponse;
import com.innitsocial.abcdish.stories.entity.Story;
import com.innitsocial.abcdish.stories.entity.StoryLike;
import com.innitsocial.abcdish.stories.entity.StoryView;
import com.innitsocial.abcdish.stories.repository.StoryLikeRepository;
import com.innitsocial.abcdish.stories.repository.StoryRepository;
import com.innitsocial.abcdish.stories.repository.StoryViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StoryService {

    private final StoryRepository storyRepository;
    private final AppUserRepository appUserRepository;
    private final ContentModerationService contentModerationService;
    private final StoryViewRepository storyViewRepository;
    private final StoryLikeRepository storyLikeRepository;
    private final ContestEntryRepository contestEntryRepository;

    @Transactional(readOnly = true)
    public List<StoryResponse> getActiveStories() {
        Optional<Long> currentUserId = SecurityUtils.currentUserIdOptional();
        return storyRepository.findTop30ByExpiresAtAfterAndModerationStatusOrderByCreatedAtDesc(
                        LocalDateTime.now(),
                        ModerationStatus.APPROVED
                )
                .stream()
                .map(story -> toResponse(story, currentUserId))
                .toList();
    }

    public StoryResponse createStory(Long userId, StoryRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ModerationResult moderation = contentModerationService.moderateFoodPost(List.of(
                clean(request.title()),
                clean(request.caption())
        ));
        ContestEntry promotedEntry = promotedContestEntry(userId, request.contestEntryId());

        Story story = storyRepository.save(Story.builder()
                .userId(userId)
                .title(request.title().trim())
                .caption(clean(request.caption()))
                .imageUrl(clean(request.imageUrl()))
                .videoUrl(clean(request.videoUrl()))
                .contestEntryId(promotedEntry == null ? null : promotedEntry.getId())
                .promotedVideoTitle(promotedEntry == null ? null : promotedEntry.getTitle())
                .moderationStatus(moderation.status())
                .moderationReason(moderation.reason())
                .build());

        return StoryResponse.fromEntity(story, user, 0, 0, false);
    }

    public void deleteStory(Long userId, Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        if (!story.getUserId().equals(userId)) {
            throw new RuntimeException("You can only remove your own story");
        }

        storyLikeRepository.deleteByStoryId(storyId);
        storyViewRepository.deleteByStoryId(storyId);
        storyRepository.delete(story);
    }

    public StoryResponse recordView(Long userId, Long storyId) {
        Story story = activeStory(storyId);

        if (!storyViewRepository.existsByStoryIdAndUserId(storyId, userId)) {
            storyViewRepository.save(StoryView.builder()
                    .storyId(storyId)
                    .userId(userId)
                    .build());
        }

        return toResponse(story, Optional.of(userId));
    }

    public StoryResponse likeStory(Long userId, Long storyId) {
        Story story = activeStory(storyId);

        if (!storyLikeRepository.existsByStoryIdAndUserId(storyId, userId)) {
            storyLikeRepository.save(StoryLike.builder()
                    .storyId(storyId)
                    .userId(userId)
                    .build());
        }

        return toResponse(story, Optional.of(userId));
    }

    public StoryResponse unlikeStory(Long userId, Long storyId) {
        Story story = activeStory(storyId);
        storyLikeRepository.deleteByStoryIdAndUserId(storyId, userId);

        return toResponse(story, Optional.of(userId));
    }

    @Transactional(readOnly = true)
    public List<StoryViewerResponse> getViewers(Long userId, Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        if (!story.getUserId().equals(userId)) {
            throw new RuntimeException("You can only see viewers for your own story");
        }

        List<StoryView> views = storyViewRepository.findByStoryIdOrderByViewedAtDesc(storyId);
        Map<Long, AppUser> usersById = appUserRepository.findAllById(
                        views.stream().map(StoryView::getUserId).toList()
                )
                .stream()
                .collect(Collectors.toMap(AppUser::getId, Function.identity()));

        return views.stream()
                .map(view -> {
                    AppUser viewer = usersById.get(view.getUserId());
                    String name = viewer == null || viewer.getName() == null || viewer.getName().isBlank()
                            ? "ABCDish Foodie"
                            : viewer.getName();

                    return new StoryViewerResponse(view.getUserId(), name, view.getViewedAt());
                })
                .toList();
    }

    private StoryResponse toResponse(Story story, Optional<Long> currentUserId) {
        AppUser user = appUserRepository.findById(story.getUserId())
                .orElseThrow(() -> new RuntimeException("Story user not found"));

        long viewCount = storyViewRepository.countByStoryId(story.getId());
        long likeCount = storyLikeRepository.countByStoryId(story.getId());
        boolean likedByCurrentUser = currentUserId
                .map(id -> storyLikeRepository.existsByStoryIdAndUserId(story.getId(), id))
                .orElse(false);

        return StoryResponse.fromEntity(story, user, viewCount, likeCount, likedByCurrentUser);
    }

    private Story activeStory(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        if (story.getExpiresAt() != null && story.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Story has expired");
        }

        return story;
    }

    private ContestEntry promotedContestEntry(Long userId, Long contestEntryId) {
        if (contestEntryId == null) {
            return null;
        }

        ContestEntry entry = contestEntryRepository.findById(contestEntryId)
                .orElseThrow(() -> new RuntimeException("Contest entry not found"));

        if (!entry.getUserId().equals(userId)) {
            throw new RuntimeException("You can only promote your own contest entry");
        }

        if (entry.getModerationStatus() != ModerationStatus.APPROVED) {
            throw new RuntimeException("Contest entry must pass moderation before story promotion");
        }

        return entry;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
