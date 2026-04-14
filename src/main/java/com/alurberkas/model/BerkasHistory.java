package com.alurberkas.model;

import com.alurberkas.model.enums.BerkasStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "berkas_history")
public class BerkasHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "berkas_id", nullable = false)
    private Berkas berkas;

    @Enumerated(EnumType.STRING)
    private BerkasStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BerkasStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Column(nullable = false)
    private String action; // "KIRIM", "KEMBALIKAN", "UPDATE", "CREATE", "APPROVE"

    @Column(columnDefinition = "TEXT")
    private String keterangan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructors
    public BerkasHistory() {}

    public BerkasHistory(Berkas berkas, BerkasStatus fromStatus, BerkasStatus toStatus,
                         User actor, String action, String keterangan) {
        this.berkas = berkas;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actor = actor;
        this.action = action;
        this.keterangan = keterangan;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Berkas getBerkas() { return berkas; }
    public void setBerkas(Berkas berkas) { this.berkas = berkas; }

    public BerkasStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(BerkasStatus fromStatus) { this.fromStatus = fromStatus; }

    public BerkasStatus getToStatus() { return toStatus; }
    public void setToStatus(BerkasStatus toStatus) { this.toStatus = toStatus; }

    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // Helper methods
    public String getFormattedTimestamp() {
        if (timestamp == null) return "";
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }

    public String getActorName() {
        return actor != null ? actor.getNama() : "-";
    }

    public String getToStatusDisplayName() {
        return toStatus != null ? toStatus.getDisplayName() : "";
    }

    public String getToStatusColor() {
        return toStatus != null ? toStatus.getColor() : "#999";
    }
}
