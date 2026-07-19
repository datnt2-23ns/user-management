package org.example.usermanagement.service;

import org.springframework.web.multipart.MultipartFile;

public interface AvatarStorageService {

    String store(MultipartFile file);

    void deleteByUrl(String avatarUrl);
}