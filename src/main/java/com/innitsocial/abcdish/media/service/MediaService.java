package com.innitsocial.abcdish.media.service;

import com.innitsocial.abcdish.media.dto.MediaUploadResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaService {
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of("mp4", "mov", "m4v", "webm");

    private final Path storagePath;
    private final String provider;
    private final String publicBaseUrl;
    private final String r2Endpoint;
    private final String r2Bucket;
    private final String r2AccessKeyId;
    private final String r2SecretAccessKey;
    private final String r2Region;

    public MediaService(
            @Value("${app.media.storage-path:uploads/media}") String storagePath,
            @Value("${app.media.provider:local}") String provider,
            @Value("${app.media.public-base-url:}") String publicBaseUrl,
            @Value("${app.media.r2.endpoint:}") String r2Endpoint,
            @Value("${app.media.r2.bucket:}") String r2Bucket,
            @Value("${app.media.r2.access-key-id:}") String r2AccessKeyId,
            @Value("${app.media.r2.secret-access-key:}") String r2SecretAccessKey,
            @Value("${app.media.r2.region:auto}") String r2Region
    ) {
        this.storagePath = Path.of(storagePath).toAbsolutePath().normalize();
        this.provider = provider == null ? "local" : provider.trim().toLowerCase(Locale.ROOT);
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
        this.r2Endpoint = trimTrailingSlash(r2Endpoint);
        this.r2Bucket = clean(r2Bucket);
        this.r2AccessKeyId = clean(r2AccessKeyId);
        this.r2SecretAccessKey = clean(r2SecretAccessKey);
        this.r2Region = clean(r2Region).isEmpty() ? "auto" : clean(r2Region);
    }

    public MediaUploadResponse createUploadPlaceholder(String fileName) {
        return new MediaUploadResponse(
                "s3-presigned-upload-url-placeholder",
                "https://cdn.abcdish.com/media/" + fileName,
                "S3 presigned upload will be connected in the production media phase."
        );
    }

    public MediaUploadResponse uploadStoryVideo(MultipartFile file, HttpServletRequest request) {
        return uploadVideo(file, request, "story", "stories", "Story video uploaded");
    }

    public MediaUploadResponse uploadRecipeTrailer(MultipartFile file, HttpServletRequest request) {
        return uploadVideo(file, request, "recipe-trailer", "recipe-trailers", "Recipe trailer uploaded");
    }

    private MediaUploadResponse uploadVideo(
            MultipartFile file,
            HttpServletRequest request,
            String filePrefix,
            String objectFolder,
            String successMessage
    ) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Video file is required");
        }

        String extension = extension(file.getOriginalFilename());
        if (!ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("Only mp4, mov, m4v, and webm videos are supported");
        }

        String fileName = filePrefix + "-" + UUID.randomUUID() + "." + extension;

        if ("r2".equals(provider) || "s3".equals(provider)) {
            return uploadToR2(file, objectFolder + "/" + fileName, successMessage + " to R2");
        }

        return uploadToLocal(file, fileName, request, successMessage + " locally");
    }

    public ResponseEntity<Resource> getFile(String fileName) {
        try {
            Path file = storagePath.resolve(fileName).normalize();
            if (!file.startsWith(storagePath) || !Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(file);
            MediaType mediaType = contentType == null
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(contentType);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException error) {
            return ResponseEntity.notFound().build();
        } catch (IOException error) {
            throw new RuntimeException("Unable to read video", error);
        }
    }

    private String extension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "mp4";
        }

        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private MediaUploadResponse uploadToLocal(
            MultipartFile file,
            String fileName,
            HttpServletRequest request,
            String message
    ) {
        try {
            Files.createDirectories(storagePath);

            Path destination = storagePath.resolve(fileName).normalize();

            if (!destination.startsWith(storagePath)) {
                throw new RuntimeException("Invalid upload path");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            String publicUrl = ServletUriComponentsBuilder
                    .fromRequestUri(request)
                    .replacePath(request.getContextPath() + "/api/media/files/" + fileName)
                    .replaceQuery(null)
                    .build()
                    .toUriString();

            return new MediaUploadResponse("", publicUrl, message);
        } catch (IOException error) {
            throw new RuntimeException("Unable to upload video", error);
        }
    }

    private MediaUploadResponse uploadToR2(MultipartFile file, String objectKey, String message) {
        validateR2Config();

        try (InputStream inputStream = file.getInputStream();
             S3Client s3Client = S3Client.builder()
                     .endpointOverride(URI.create(r2Endpoint))
                     .region(Region.of(r2Region))
                     .credentialsProvider(StaticCredentialsProvider.create(
                             AwsBasicCredentials.create(r2AccessKeyId, r2SecretAccessKey)
                     ))
                     .httpClientBuilder(UrlConnectionHttpClient.builder())
                     .forcePathStyle(true)
                     .build()) {

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(r2Bucket)
                    .key(objectKey)
                    .contentType(contentType(file))
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));

            return new MediaUploadResponse("", publicBaseUrl + "/" + objectKey, message);
        } catch (IOException error) {
            throw new RuntimeException("Unable to upload video", error);
        }
    }

    private void validateR2Config() {
        if (r2Endpoint.isEmpty() ||
                r2Bucket.isEmpty() ||
                r2AccessKeyId.isEmpty() ||
                r2SecretAccessKey.isEmpty() ||
                publicBaseUrl.isEmpty()) {
            throw new RuntimeException("R2 storage is not configured");
        }
    }

    private String contentType(MultipartFile file) {
        String contentType = clean(file.getContentType());
        return contentType.isEmpty() ? "video/mp4" : contentType;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimTrailingSlash(String value) {
        String cleaned = clean(value);
        while (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return cleaned;
    }
}
