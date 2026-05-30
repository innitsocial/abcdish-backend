package com.innitsocial.abcdish.feed.controller;

import com.innitsocial.abcdish.feed.dto.FeedItemResponse;
import com.innitsocial.abcdish.feed.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public List<FeedItemResponse> getHomeFeed() {
        return feedService.getHomeFeed();
    }
}
