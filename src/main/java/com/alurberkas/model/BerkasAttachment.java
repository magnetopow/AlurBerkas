package com.alurberkas.model;

import com.alurberkas.model.enums.BerkasStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "berkas_attachment")
public class BerkasAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "berkas_id", nullable = false)
    private Berkas berkas;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String storedFileName;

    @Column(nullable = false)
    private String filePath;

    private String fileType; // MIME type
    private Long fileSize;   // in bytes

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id")
    private User uploadedBy;

    @Enumerated(EnumType.STRING)
    private BerkasStatus stage; // Which workflow stage this was uploaded at

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    // Constructors
    public BerkasAttachment() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Berkas getBerkas() { return berkas; }
    public void setBerkas(Berkas berkas) { this.berkas = berkas; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public BerkasStatus getStage() { return stage; }
    public void setStage(BerkasStatus stage) { this.stage = stage; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    // Helper methods
    public String getFormattedUploadedAt() {
        if (uploadedAt == null) return "";
        return uploadedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }

    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024));
    }

    public String getUploaderName() {
        return uploadedBy != null ? uploadedBy.getNama() : "-";
    }

    public boolean isImage() {
        return fileType != null && fileType.startsWith("image/");
    }

    public boolean isPdf() {
        return fileType != null && fileType.equals("application/pdf");
    }
}
