package com.scheduler.backend.global.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class LocalProfileImageStorageService implements ProfileImageStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${app.upload.profile-image-dir:./uploads/profile-images}")
    private String uploadDir;

    @Value("${app.upload.base-url:/uploads}")
    private String baseUrl;

    @Override
    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("파일 크기는 5MB 이하여야 합니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (jpg, jpeg, png, webp 만 허용)");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다.");
        }

        String filename = UUID.randomUUID() + "." + ext;

        try {
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path dest = dir.resolve(filename);
            file.transferTo(dest.toFile());
            return baseUrl + "/profile-images/" + filename;
        } catch (IOException e) {
            log.error("프로필 이미지 저장 실패", e);
            throw new RuntimeException("파일 저장에 실패했습니다.");
        }
    }

    @Override
    public void delete(String url) {
        if (url == null) return;
        try {
            String prefix = baseUrl + "/profile-images/";
            if (url.startsWith(prefix)) {
                String filename = url.substring(prefix.length());
                Path filePath = Paths.get(uploadDir).resolve(filename);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            log.warn("프로필 이미지 삭제 실패: {}", url, e);
        }
    }
}
