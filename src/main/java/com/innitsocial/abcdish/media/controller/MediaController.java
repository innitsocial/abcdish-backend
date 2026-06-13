package com.innitsocial.abcdish.media.controller;

import com.innitsocial.abcdish.media.dto.MediaUploadResponse;
import com.innitsocial.abcdish.media.service.MediaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @GetMapping("/upload-url")
    public MediaUploadResponse createUploadUrl(@RequestParam String fileName) {
        return mediaService.createUploadPlaceholder(fileName);
    }

    @PostMapping("/story-video")
    public MediaUploadResponse uploadStoryVideo(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        return mediaService.uploadStoryVideo(file, request);
    }

    @PostMapping("/recipe-trailer")
    public MediaUploadResponse uploadRecipeTrailer(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        return mediaService.uploadRecipeTrailer(file, request);
    }

    @GetMapping("/files/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) {
        return mediaService.getFile(fileName);
    }
}
