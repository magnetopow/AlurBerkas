package com.alurberkas.model.enums;

public enum Role {
    ADMIN("Administrasi", "Menerima & input berkas baru"),
    PETUGAS_UKUR("Petugas Ukur", "Pengukuran lapangan"),
    PEMETAAN("Pemetaan", "Membuat peta bidang tanah"),
    KASUBSI("Kasubsi 1", "Review & verifikasi"),
    KASI("Kasi", "Final approval & tanda tangan digital"),
    SUPER_ADMIN("Super Admin", "Akses penuh + user management");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
