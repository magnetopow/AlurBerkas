package com.alurberkas.model.enums;

public enum BerkasStatus {
    DITERIMA("Diterima", "Berkas diterima oleh Administrasi", "#42A5F5", "fa-inbox", Role.ADMIN),
    PENGUKURAN("Pengukuran", "Dalam proses pengukuran lapangan", "#FFA726", "fa-ruler-combined", Role.PETUGAS_UKUR),
    PEMETAAN("Pemetaan", "Dalam proses pemetaan", "#AB47BC", "fa-map", Role.PEMETAAN),
    VERIFIKASI("Verifikasi", "Dalam review Kasubsi", "#26C6DA", "fa-check-double", Role.KASUBSI),
    APPROVAL("Approval", "Menunggu approval Kasi", "#66BB6A", "fa-stamp", Role.KASI),
    SELESAI("Selesai", "Berkas selesai diproses", "#4CAF50", "fa-archive", null),
    DIKEMBALIKAN("Dikembalikan", "Berkas dikembalikan untuk revisi", "#EF5350", "fa-undo", null);

    private final String displayName;
    private final String description;
    private final String color;
    private final String icon;
    private final Role handlerRole;

    BerkasStatus(String displayName, String description, String color, String icon, Role handlerRole) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
        this.icon = icon;
        this.handlerRole = handlerRole;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getColor() { return color; }
    public String getIcon() { return icon; }
    public Role getHandlerRole() { return handlerRole; }

    public BerkasStatus getNextStatus() {
        return switch (this) {
            case DITERIMA -> PENGUKURAN;
            case PENGUKURAN -> PEMETAAN;
            case PEMETAAN -> VERIFIKASI;
            case VERIFIKASI -> APPROVAL;
            case APPROVAL -> SELESAI;
            default -> null;
        };
    }

    public BerkasStatus getPreviousStatus() {
        return switch (this) {
            case PENGUKURAN -> DITERIMA;
            case PEMETAAN -> PENGUKURAN;
            case VERIFIKASI -> PEMETAAN;
            case APPROVAL -> VERIFIKASI;
            default -> null;
        };
    }
}
