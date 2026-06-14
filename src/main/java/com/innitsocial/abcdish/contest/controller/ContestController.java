package com.innitsocial.abcdish.contest.controller;

import com.innitsocial.abcdish.common.security.SecurityUtils;
import com.innitsocial.abcdish.contest.dto.ContestEntryRequest;
import com.innitsocial.abcdish.contest.dto.ContestEntryResponse;
import com.innitsocial.abcdish.contest.dto.ContestResponse;
import com.innitsocial.abcdish.contest.service.ContestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;

    @GetMapping
    public List<ContestResponse> openContests() {
        return contestService.findOpenContests();
    }

    @GetMapping("/my/entries")
    public List<ContestEntryResponse> myApprovedEntries() {
        return contestService.myApprovedEntries(SecurityUtils.currentUserId());
    }

    @GetMapping("/{contestId}/entries")
    public List<ContestEntryResponse> approvedEntries(@PathVariable Long contestId) {
        return contestService.approvedEntries(contestId, SecurityUtils.currentUserIdOptional());
    }

    @PostMapping("/{contestId}/entries")
    public ContestEntryResponse submitEntry(
            @PathVariable Long contestId,
            @Valid @RequestBody ContestEntryRequest request
    ) {
        return contestService.submitEntry(SecurityUtils.currentUserId(), contestId, request);
    }

    @PostMapping("/entries/{entryId}/likes")
    public ContestEntryResponse likeEntry(@PathVariable Long entryId) {
        return contestService.likeEntry(SecurityUtils.currentUserId(), entryId);
    }

    @DeleteMapping("/entries/{entryId}/likes")
    public ContestEntryResponse unlikeEntry(@PathVariable Long entryId) {
        return contestService.unlikeEntry(SecurityUtils.currentUserId(), entryId);
    }

    @PostMapping("/entries/{entryId}/accept")
    public ContestEntryResponse acceptEntry(@PathVariable Long entryId) {
        return contestService.acceptEntry(entryId);
    }

    @PostMapping("/entries/{entryId}/winner")
    public ContestEntryResponse selectWinner(@PathVariable Long entryId) {
        return contestService.selectWinner(entryId);
    }
}
