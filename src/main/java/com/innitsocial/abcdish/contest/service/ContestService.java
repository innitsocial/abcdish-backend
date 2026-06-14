package com.innitsocial.abcdish.contest.service;

import com.innitsocial.abcdish.contest.dto.ContestEntryRequest;
import com.innitsocial.abcdish.contest.dto.ContestEntryResponse;
import com.innitsocial.abcdish.contest.dto.ContestResponse;
import com.innitsocial.abcdish.contest.entity.ContestEntry;
import com.innitsocial.abcdish.contest.entity.ContestEntryLike;
import com.innitsocial.abcdish.contest.entity.ContestStatus;
import com.innitsocial.abcdish.contest.repository.ContestEntryLikeRepository;
import com.innitsocial.abcdish.contest.repository.ContestEntryRepository;
import com.innitsocial.abcdish.contest.repository.ContestRepository;
import com.innitsocial.abcdish.content.entity.Meal;
import com.innitsocial.abcdish.content.repository.MealRepository;
import com.innitsocial.abcdish.content.service.CatalogCapacityService;
import com.innitsocial.abcdish.moderation.ContentModerationService;
import com.innitsocial.abcdish.moderation.ModerationResult;
import com.innitsocial.abcdish.moderation.ModerationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContestService {

    private final ContestRepository contestRepository;
    private final ContestEntryRepository contestEntryRepository;
    private final ContestEntryLikeRepository contestEntryLikeRepository;
    private final MealRepository mealRepository;
    private final ContentModerationService contentModerationService;
    private final CatalogCapacityService catalogCapacityService;

    @Value("${app.contests.acceptance-like-threshold:500}")
    private long acceptanceLikeThreshold;

    @Value("${app.contests.london-finalist-limit:100}")
    private int londonFinalistLimit;

    @Value("${app.contests.category-winner-prize-gbp:100000}")
    private int categoryWinnerPrizeGbp;

    @Transactional(readOnly = true)
    public List<ContestResponse> findOpenContests() {
        return contestRepository.findActiveByStatus(ContestStatus.OPEN, LocalDateTime.now())
                .stream()
                .map(ContestResponse::fromEntity)
                .toList();
    }

    public ContestEntryResponse submitEntry(Long userId, Long contestId, ContestEntryRequest request) {
        contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found"));

        if (!request.soundFreeConfirmed()) {
            throw new RuntimeException("Contest videos must be sound-free so ABCDish can add trusted AI narration after review");
        }

        ModerationResult moderation = contentModerationService.moderateFoodPost(List.of(
                clean(request.title()),
                clean(request.description()),
                String.join(" ", safeList(request.categories())),
                String.join(" ", safeList(request.ingredients())),
                String.join(" ", safeList(request.steps()))
        ));

        ContestEntry entry = ContestEntry.builder()
                .contestId(contestId)
                .userId(userId)
                .title(request.title())
                .description(request.description())
                .videoUrl(request.videoUrl())
                .thumbnailUrl(request.thumbnailUrl())
                .approved(false)
                .duration(request.duration() == null ? 30 : request.duration())
                .complexity(blankToDefault(request.complexity(), "simple"))
                .competitionCategory(blankToDefault(request.competitionCategory(), "main").toLowerCase())
                .eligibleForVoting(false)
                .competitionStatus("PENDING_ADMIN_REVIEW")
                .glutenFree(request.glutenFree())
                .lactoseFree(request.lactoseFree())
                .vegan(request.vegan())
                .vegetarian(request.vegetarian())
                .soundFreeConfirmed(request.soundFreeConfirmed())
                .aiNarrationRequested(request.aiNarrationRequested())
                .narrationStatus(request.aiNarrationRequested() ? "PENDING_REVIEW" : "NOT_REQUESTED")
                .moderationStatus(moderation.status())
                .moderationReason(moderation.reason())
                .votes(0)
                .build();

        return toResponse(contestEntryRepository.save(entry), Optional.of(userId));
    }

    @Transactional(readOnly = true)
    public List<ContestEntryResponse> approvedEntries(Long contestId, Optional<Long> currentUserId) {
        return contestEntryRepository.findVisibleEntriesForContest(
                        contestId,
                        ModerationStatus.APPROVED,
                        LocalDateTime.now()
                )
                .stream()
                .map(entry -> toResponse(entry, currentUserId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContestEntryResponse> myApprovedEntries(Long userId) {
        return contestEntryRepository
                .findByUserIdAndModerationStatusOrderByCreatedAtDesc(userId, ModerationStatus.APPROVED)
                .stream()
                .map(entry -> toResponse(entry, Optional.of(userId)))
                .toList();
    }

    public ContestEntryResponse likeEntry(Long userId, Long entryId) {
        ContestEntry entry = findEntryForVoting(entryId);

        if (!contestEntryLikeRepository.existsByEntryIdAndUserId(entryId, userId)) {
            contestEntryLikeRepository.save(ContestEntryLike.builder()
                    .entryId(entryId)
                    .userId(userId)
                    .build());
        }

        return refreshVotes(entry, Optional.of(userId));
    }

    public ContestEntryResponse unlikeEntry(Long userId, Long entryId) {
        ContestEntry entry = findEntryForVoting(entryId);
        contestEntryLikeRepository.deleteByEntryIdAndUserId(entryId, userId);
        return refreshVotes(entry, Optional.of(userId));
    }

    public ContestEntryResponse acceptEntry(Long entryId) {
        ContestEntry entry = contestEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Contest entry not found"));

        if (entry.getModerationStatus() != ModerationStatus.APPROVED) {
            throw new RuntimeException("Contest entry must pass moderation before admin acceptance");
        }

        entry.setApproved(true);
        entry.setEligibleForVoting(true);
        entry.setCompetitionStatus("VOTING");
        refreshFinalists(entry.getContestId());

        return toResponse(contestEntryRepository.save(entry), Optional.empty());
    }

    public ContestEntryResponse selectWinner(Long entryId) {
        ContestEntry entry = contestEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Contest entry not found"));

        if (!entry.isEligibleForVoting()) {
            throw new RuntimeException("Contest entry must be accepted for voting before winner selection");
        }

        boolean existingWinnerInCategory = contestEntryRepository
                .findByContestIdAndEligibleForVotingTrueOrderByVotesDesc(entry.getContestId())
                .stream()
                .anyMatch(other -> other.getId() != null
                        && !other.getId().equals(entry.getId())
                        && "WINNER".equals(other.getCompetitionStatus())
                        && clean(other.getCompetitionCategory()).equals(clean(entry.getCompetitionCategory())));
        if (existingWinnerInCategory) {
            throw new RuntimeException("This category already has a winner");
        }

        Meal meal = promoteToMeal(entry);
        entry.setApproved(true);
        entry.setEligibleForVoting(true);
        entry.setCompetitionStatus("WINNER");
        entry.setLondonQualified(true);
        entry.setPrizeAmountGbp(categoryWinnerPrizeGbp);
        entry.setWinnerSelectedAt(LocalDateTime.now());
        entry.setAcceptedMealId(meal.getId());
        entry.setAcceptedAt(LocalDateTime.now());

        return toResponse(contestEntryRepository.save(entry), Optional.empty());
    }

    private ContestEntry findEntryForVoting(Long entryId) {
        ContestEntry entry = contestEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Contest entry not found"));

        if (entry.getModerationStatus() != ModerationStatus.APPROVED) {
            throw new RuntimeException("Contest entry is not open for voting yet");
        }

        return entry;
    }

    private ContestEntryResponse refreshVotesAndMaybePromote(ContestEntry entry, Optional<Long> currentUserId) {
        return refreshVotes(entry, currentUserId);
    }

    private ContestEntryResponse refreshVotes(ContestEntry entry, Optional<Long> currentUserId) {
        long voteCount = contestEntryLikeRepository.countByEntryId(entry.getId());
        entry.setVotes(voteCount);
        refreshFinalists(entry.getContestId());

        return toResponse(contestEntryRepository.save(entry), currentUserId);
    }

    private void refreshFinalists(Long contestId) {
        List<ContestEntry> entries = contestEntryRepository.findByContestIdAndEligibleForVotingTrueOrderByVotesDesc(contestId);
        for (int index = 0; index < entries.size(); index++) {
            ContestEntry entry = entries.get(index);
            boolean qualified = index < londonFinalistLimit;
            entry.setFinalistRank(qualified ? index + 1 : null);
            entry.setLondonQualified(qualified || "WINNER".equals(entry.getCompetitionStatus()));
            if (!"WINNER".equals(entry.getCompetitionStatus())) {
                entry.setCompetitionStatus(qualified ? "LONDON_FINALIST" : "VOTING");
            }
        }
        contestEntryRepository.saveAll(entries);
    }

    private Meal promoteToMeal(ContestEntry entry) {
        if (entry.getAcceptedMealId() != null) {
            return mealRepository.findById(entry.getAcceptedMealId())
                    .orElseThrow(() -> new RuntimeException("Accepted meal not found"));
        }

        catalogCapacityService.ensureCapacityAvailable();

        Meal meal = Meal.builder()
                .title(entry.getTitle())
                .description(clean(entry.getDescription()).isBlank()
                        ? "Community accepted contest recipe."
                        : entry.getDescription())
                .imageUrl(entry.getThumbnailUrl())
                .videoUrl(entry.getVideoUrl())
                .trailerUrl(entry.getVideoUrl())
                .trailerType("VIDEO")
                .promoTrailerTitle(entry.getTitle())
                .promoTrailerSubtitle("Community accepted recipe")
                .duration(entry.getDuration() == null ? 30 : entry.getDuration())
                .complexity(blankToDefault(entry.getComplexity(), "simple"))
                .affordability("affordable")
                .categories(List.of("contest"))
                .ingredients(List.of("See cooking video"))
                .steps(List.of("Watch the accepted contest video and cook along."))
                .glutenFree(entry.isGlutenFree())
                .lactoseFree(entry.isLactoseFree())
                .vegan(entry.isVegan())
                .vegetarian(entry.isVegetarian())
                .moderationStatus(ModerationStatus.APPROVED)
                .moderationReason("Accepted from contest after admin due diligence")
                .build();

        Meal savedMeal = mealRepository.save(meal);
        ensureRecipeCode(savedMeal);
        return mealRepository.save(savedMeal);
    }

    private ContestEntryResponse toResponse(ContestEntry entry, Optional<Long> currentUserId) {
        boolean liked = currentUserId
                .map(userId -> contestEntryLikeRepository.existsByEntryIdAndUserId(entry.getId(), userId))
                .orElse(false);
        long voteCount = contestEntryLikeRepository.countByEntryId(entry.getId());
        if (entry.getVotes() != voteCount) {
            entry.setVotes(voteCount);
        }

        return ContestEntryResponse.fromEntity(entry, liked, acceptanceLikeThreshold);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private String blankToDefault(String value, String fallback) {
        String cleaned = clean(value);
        return cleaned.isBlank() ? fallback : cleaned;
    }

    private void ensureRecipeCode(Meal meal) {
        if (meal.getRecipeCode() != null && !meal.getRecipeCode().isBlank()) {
            return;
        }

        Long id = meal.getId();
        if (id == null) {
            return;
        }

        long candidate = 10000 + id;
        while (mealRepository.findByRecipeCode(String.valueOf(candidate)).isPresent()) {
            candidate++;
        }

        meal.setRecipeCode(String.valueOf(candidate));
    }
}
