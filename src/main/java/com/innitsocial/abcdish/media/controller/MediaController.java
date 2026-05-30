package com.innitsocial.abcdish.media.controller;

import com.innitsocial.abcdish.media.dto.MediaUploadResponse;
import com.innitsocial.abcdish.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @GetMapping("/upload-url")
    public MediaUploadResponse createUploadUrl(@RequestParam String fileName) {
        return mediaService.createUploadPlaceholder(fileName);
    }
}
