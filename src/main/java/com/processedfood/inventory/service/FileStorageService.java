package com.processedfood.inventory.service;

import com.processedfood.inventory.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private final Path uploadPath = Paths.get("src/main/resources/static/uploads");

    public String storeProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BadRequestException("Image size must be <= 2MB");
        }

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BadRequestException("Only jpg, jpeg, png files are allowed");
        }

        try {
            Files.createDirectories(uploadPath);
            String fileName = UUID.randomUUID() + "." + ext;
            Path target = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new BadRequestException("Failed to save image");
        }
    }
}
