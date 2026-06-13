package com.innitsocial.abcdish.contest.service;

import com.innitsocial.abcdish.contest.dto.ContestEntryRequest;
import com.innitsocial.abcdish.contest.dto.ContestEntryResponse;
import com.innitsocial.abcdish.contest.dto.ContestResponse;
import com.innitsocial.abcdish.contest.entity.ContestEntry;
import com.innitsocial.abcdish.contest.entity.ContestStatus;
import com.innitsocial.abcdish.contest.repository.ContestEntryRepository;
import com.innitsocial.abcdish.contest.repository.ContestRepository;
import com.innitsocial.abcdish.moderation.ContentModerationService;
import com.innitsocial.abcdish.moderation.ModerationResult;
import com.innitsocial.abcdish.moderation.ModerationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContestService {

    private final ContestRepository contestRepository;
    private final ContestEntryRepository contestEntryRepository;
    private final ContentModerationService contentModerationService;

    @Transactional(readOnly = true)
    public List<ContestResponse> findOpenContests() {
        return contestRepository.findByStatus(ContestStatus.OPEN)
                .stream()
                .map(ContestResponse::fromEntity)
                .toList();
    }

    public ContestEntryResponse submitEntry(Long userId, Long contestId, ContestEntryRequest request) {
        contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found"));

        ModerationResult moderation = contentModerationService.moderateFoodPost(List.of(
                clean(request.title()),
                clean(request.description())
        ));

        ContestEntry entry = ContestEntry.builder()
                .contestId(contestId)
                .userId(userId)
                .title(request.title())
                .description(request.description())
                .videoUrl(request.videoUrl())
                .thumbnailUrl(request.thumbnailUrl())
                .approved(moderation.status() == ModerationStatus.APPROVED)
                .moderationStatus(moderation.status())
                .moderationReason(moderation.reason())
                .votes(0)
                .build();

        return ContestEntryResponse.fromEntity(contestEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public List<ContestEntryResponse> approvedEntries(Long contestId) {
        return contestEntryRepository.findByContestIdAndModerationStatus(contestId, ModerationStatus.APPROVED)
                .stream()
                .map(ContestEntryResponse::fromEntity)
                .toList();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
