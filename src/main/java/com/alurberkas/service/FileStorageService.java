package com.alurberkas.service;

import com.alurberkas.model.Berkas;
import com.alurberkas.model.BerkasAttachment;
import com.alurberkas.model.User;
import com.alurberkas.model.enums.BerkasStatus;
import com.alurberkas.repository.BerkasAttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;
    private final BerkasAttachmentRepository attachmentRepository;

    public FileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir,
                              BerkasAttachmentRepository attachmentRepository) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.attachmentRepository = attachmentRepository;
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Gagal membuat direktori upload: " + uploadDir, e);
        }
    }

    @Transactional
    public BerkasAttachment storeFile(MultipartFile file, Berkas berkas, User uploader,
                                      BerkasStatus stage, String description) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File kosong");
        }

        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String storedFileName = UUID.randomUUID().toString() + extension;
        Path targetPath = this.uploadDir.resolve(storedFileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        BerkasAttachment attachment = new BerkasAttachment();
        attachment.setBerkas(berkas);
        attachment.setFileName(originalFileName);
        attachment.setStoredFileName(storedFileName);
        attachment.setFilePath(targetPath.toString());
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setUploadedBy(uploader);
        attachment.setStage(stage);
        attachment.setDescription(description);

        return attachmentRepository.save(attachment);
    }

    public Path getFilePath(String storedFileName) {
        return uploadDir.resolve(storedFileName);
    }

    public List<BerkasAttachment> getAttachments(Long berkasId) {
        return attachmentRepository.findByBerkasIdOrderByUploadedAtDesc(berkasId);
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) throws IOException {
        BerkasAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment tidak ditemukan"));

        Path filePath = Paths.get(attachment.getFilePath());
        Files.deleteIfExists(filePath);

        attachmentRepository.delete(attachment);
    }
}
