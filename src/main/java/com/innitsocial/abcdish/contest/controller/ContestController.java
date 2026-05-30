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

    @GetMapping("/{contestId}/entries")
    public List<ContestEntryResponse> approvedEntries(@PathVariable Long contestId) {
        return contestService.approvedEntries(contestId);
    }

    @PostMapping("/{contestId}/entries")
    public ContestEntryResponse submitEntry(
            @PathVariable Long contestId,
            @Valid @RequestBody ContestEntryRequest request
    ) {
        return contestService.submitEntry(SecurityUtils.currentUserId(), contestId, request);
    }
}
