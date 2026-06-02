package com.scheduler.backend.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStorageService {
    String save(MultipartFile file);
    void delete(String url);
}
