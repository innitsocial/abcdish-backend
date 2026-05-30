package com.innitsocial.abcdish.media.service;

import com.innitsocial.abcdish.media.dto.MediaUploadResponse;
import org.springframework.stereotype.Service;

@Service
public class MediaService {

    public MediaUploadResponse createUploadPlaceholder(String fileName) {
        return new MediaUploadResponse(
                "s3-presigned-upload-url-placeholder",
                "https://cdn.abcdish.com/media/" + fileName,
                "S3 presigned upload will be connected in the production media phase."
        );
    }
}
