package org.example.usermanagement.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.usermanagement.exception.AvatarStorageException;
import org.example.usermanagement.exception.InvalidAvatarException;
import org.example.usermanagement.service.AvatarStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
@Slf4j
public class AvatarStorageServiceImpl
        implements AvatarStorageService {

    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89,
            0x50,
            0x4E,
            0x47,
            0x0D,
            0x0A,
            0x1A,
            0x0A
    };

    private final Path avatarDirectory;
    private final String avatarUrlPrefix;
    private final long maximumFileSize;

    public AvatarStorageServiceImpl(
            @Value("${app.upload.avatar-dir:uploads/avatars}") String avatarDirectory,

            @Value("${app.upload.avatar-url-prefix:/uploads/avatars}") String avatarUrlPrefix,

            @Value("${app.upload.avatar-max-size:5242880}") long maximumFileSize) {
        this.avatarDirectory = Path
                .of(avatarDirectory)
                .toAbsolutePath()
                .normalize();

        this.avatarUrlPrefix = normalizeUrlPrefix(avatarUrlPrefix);

        this.maximumFileSize = maximumFileSize;

        createStorageDirectory();
    }

    @Override
    public String store(MultipartFile file) {
        validateBasicInformation(file);

        byte[] content = readContent(file);
        String extension = detectExtension(content);

        validateImageContent(content);

        String filename = UUID.randomUUID() + extension;

        Path targetPath = avatarDirectory
                .resolve(filename)
                .normalize();

        if (!targetPath.startsWith(avatarDirectory)) {
            throw new InvalidAvatarException(
                    "Tên file ảnh đại diện không hợp lệ");
        }

        try {
            Files.write(
                    targetPath,
                    content,
                    StandardOpenOption.CREATE_NEW);
        } catch (IOException exception) {
            throw new AvatarStorageException(
                    "Không thể lưu ảnh đại diện",
                    exception);
        }

        return avatarUrlPrefix + "/" + filename;
    }

    @Override
    public void deleteByUrl(String avatarUrl) {
        if (avatarUrl == null
                || avatarUrl.isBlank()
                || !avatarUrl.startsWith(
                        avatarUrlPrefix + "/")) {
            return;
        }

        String filename = avatarUrl.substring(
                avatarUrl.lastIndexOf('/') + 1);

        if (filename.isBlank()) {
            return;
        }

        Path filePath = avatarDirectory
                .resolve(filename)
                .normalize();

        if (!filePath.startsWith(avatarDirectory)) {
            log.warn(
                    "Không thể xóa file nằm ngoài thư mục avatar: {}",
                    filePath);
            return;
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            log.warn(
                    "Không thể xóa ảnh đại diện cũ: {}",
                    filePath,
                    exception);
        }
    }

    private void validateBasicInformation(
            MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidAvatarException(
                    "Vui lòng chọn ảnh đại diện");
        }

        if (file.getSize() > maximumFileSize) {
            throw new InvalidAvatarException(
                    "Ảnh đại diện không được vượt quá 2 MB");
        }
    }

    private byte[] readContent(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new AvatarStorageException(
                    "Không thể đọc file ảnh đại diện",
                    exception);
        }
    }

    private String detectExtension(byte[] content) {
        if (isPng(content)) {
            return ".png";
        }

        if (isJpeg(content)) {
            return ".jpg";
        }

        throw new InvalidAvatarException(
                "Chỉ chấp nhận ảnh JPG hoặc PNG");
    }

    private boolean isPng(byte[] content) {
        if (content.length < PNG_SIGNATURE.length) {
            return false;
        }

        for (int index = 0; index < PNG_SIGNATURE.length; index++) {

            if (content[index] != PNG_SIGNATURE[index]) {
                return false;
            }
        }

        return true;
    }

    private boolean isJpeg(byte[] content) {
        return content.length >= 3
                && content[0] == (byte) 0xFF
                && content[1] == (byte) 0xD8
                && content[2] == (byte) 0xFF;
    }

    private void validateImageContent(byte[] content) {
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
            if (ImageIO.read(inputStream) == null) {
                throw new InvalidAvatarException(
                        "Nội dung file ảnh không hợp lệ");
            }
        } catch (IOException exception) {
            throw new InvalidAvatarException(
                    "Nội dung file ảnh không hợp lệ");
        }
    }

    private void createStorageDirectory() {
        try {
            Files.createDirectories(avatarDirectory);
        } catch (IOException exception) {
            throw new AvatarStorageException(
                    "Không thể khởi tạo thư mục lưu ảnh đại diện",
                    exception);
        }
    }

    private String normalizeUrlPrefix(
            String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "/uploads/avatars";
        }

        String normalized = prefix.trim();

        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        while (normalized.length() > 1
                && normalized.endsWith("/")) {
            normalized = normalized.substring(
                    0,
                    normalized.length() - 1);
        }

        return normalized;
    }
}