package com.alurberkas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "berkas_id")
    private Berkas berkas;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String type; // INFO, WARNING, SUCCESS, ERROR

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructors
    public Notification() {}

    public Notification(User user, Berkas berkas, String title, String message, String type) {
        this.user = user;
        this.berkas = berkas;
        this.title = title;
        this.message = message;
        this.type = type;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Berkas getBerkas() { return berkas; }
    public void setBerkas(Berkas berkas) { this.berkas = berkas; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }

    public String getTimeAgo() {
        if (createdAt == null) return "";
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(createdAt, now).toMinutes();
        if (minutes < 1) return "Baru saja";
        if (minutes < 60) return minutes + " menit lalu";
        long hours = minutes / 60;
        if (hours < 24) return hours + " jam lalu";
        long days = hours / 24;
        if (days < 30) return days + " hari lalu";
        return getFormattedCreatedAt();
    }

    public Long getBerkasId() {
        return berkas != null ? berkas.getId() : null;
    }
}
