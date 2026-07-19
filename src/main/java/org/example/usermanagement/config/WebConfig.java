package org.example.usermanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

        private final Path avatarDirectory;

        public WebConfig(
                        @Value("${app.upload.avatar-dir:uploads/avatars}") String avatarDirectory) {
                this.avatarDirectory = Path
                                .of(avatarDirectory)
                                .toAbsolutePath()
                                .normalize();

                createAvatarDirectory();
        }

        @Override
        public void addResourceHandlers(
                        ResourceHandlerRegistry registry) {
                String resourceLocation = avatarDirectory.toUri().toString();

                if (!resourceLocation.endsWith("/")) {
                        resourceLocation += "/";
                }

                registry
                                .addResourceHandler(
                                                "/uploads/avatars/**")
                                .addResourceLocations(
                                                resourceLocation)
                                .setCachePeriod(0);

                log.info(
                                "Avatar resource mapping: /uploads/avatars/** -> {}",
                                resourceLocation);
        }

        private void createAvatarDirectory() {
                try {
                        Files.createDirectories(
                                        avatarDirectory);

                        log.info(
                                        "Avatar storage directory: {}",
                                        avatarDirectory);
                } catch (IOException exception) {
                        throw new IllegalStateException(
                                        "Không thể khởi tạo thư mục lưu ảnh đại diện",
                                        exception);
                }
        }
}