package com.alurberkas.model;

import com.alurberkas.model.enums.BerkasStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "berkas")
public class Berkas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String noBerkas;

    // Data Pemohon
    @Column(nullable = false)
    private String namaPemohon;

    private String nikPemohon;
    private String noHpPemohon;
    private String alamatPemohon;

    // Data Tanah
    @Column(nullable = false)
    private String alamatTanah;

    private String kelurahan;
    private String kecamatan;
    private Double luasTanah;
    private String jenisPermohonan;

    // Workflow
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BerkasStatus status = BerkasStatus.DITERIMA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_handler_id")
    private User currentHandler;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    // Data Pengukuran (diisi Petugas Ukur)
    @Column(columnDefinition = "TEXT")
    private String koordinatTanah;

    private Double hasilLuasUkur;

    @Column(columnDefinition = "TEXT")
    private String catatanUkur;

    private LocalDate tanggalUkur;

    // Data Pemetaan (diisi Pemetaan)
    @Column(columnDefinition = "TEXT")
    private String catatanPeta;

    private String noPetaBidang;

    // Catatan umum
    @Column(columnDefinition = "TEXT")
    private String catatan;

    @Column(columnDefinition = "TEXT")
    private String alasanPengembalian;

    // Timestamps
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    // Relationships
    @OneToMany(mappedBy = "berkas", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp DESC")
    private List<BerkasHistory> histories = new ArrayList<>();

    @OneToMany(mappedBy = "berkas", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("uploadedAt DESC")
    private List<BerkasAttachment> attachments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Berkas() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNoBerkas() { return noBerkas; }
    public void setNoBerkas(String noBerkas) { this.noBerkas = noBerkas; }

    public String getNamaPemohon() { return namaPemohon; }
    public void setNamaPemohon(String namaPemohon) { this.namaPemohon = namaPemohon; }

    public String getNikPemohon() { return nikPemohon; }
    public void setNikPemohon(String nikPemohon) { this.nikPemohon = nikPemohon; }

    public String getNoHpPemohon() { return noHpPemohon; }
    public void setNoHpPemohon(String noHpPemohon) { this.noHpPemohon = noHpPemohon; }

    public String getAlamatPemohon() { return alamatPemohon; }
    public void setAlamatPemohon(String alamatPemohon) { this.alamatPemohon = alamatPemohon; }

    public String getAlamatTanah() { return alamatTanah; }
    public void setAlamatTanah(String alamatTanah) { this.alamatTanah = alamatTanah; }

    public String getKelurahan() { return kelurahan; }
    public void setKelurahan(String kelurahan) { this.kelurahan = kelurahan; }

    public String getKecamatan() { return kecamatan; }
    public void setKecamatan(String kecamatan) { this.kecamatan = kecamatan; }

    public Double getLuasTanah() { return luasTanah; }
    public void setLuasTanah(Double luasTanah) { this.luasTanah = luasTanah; }

    public String getJenisPermohonan() { return jenisPermohonan; }
    public void setJenisPermohonan(String jenisPermohonan) { this.jenisPermohonan = jenisPermohonan; }

    public BerkasStatus getStatus() { return status; }
    public void setStatus(BerkasStatus status) { this.status = status; }

    public User getCurrentHandler() { return currentHandler; }
    public void setCurrentHandler(User currentHandler) { this.currentHandler = currentHandler; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public String getKoordinatTanah() { return koordinatTanah; }
    public void setKoordinatTanah(String koordinatTanah) { this.koordinatTanah = koordinatTanah; }

    public Double getHasilLuasUkur() { return hasilLuasUkur; }
    public void setHasilLuasUkur(Double hasilLuasUkur) { this.hasilLuasUkur = hasilLuasUkur; }

    public String getCatatanUkur() { return catatanUkur; }
    public void setCatatanUkur(String catatanUkur) { this.catatanUkur = catatanUkur; }

    public LocalDate getTanggalUkur() { return tanggalUkur; }
    public void setTanggalUkur(LocalDate tanggalUkur) { this.tanggalUkur = tanggalUkur; }

    public String getCatatanPeta() { return catatanPeta; }
    public void setCatatanPeta(String catatanPeta) { this.catatanPeta = catatanPeta; }

    public String getNoPetaBidang() { return noPetaBidang; }
    public void setNoPetaBidang(String noPetaBidang) { this.noPetaBidang = noPetaBidang; }

    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    public String getAlasanPengembalian() { return alasanPengembalian; }
    public void setAlasanPengembalian(String alasanPengembalian) { this.alasanPengembalian = alasanPengembalian; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public List<BerkasHistory> getHistories() { return histories; }
    public void setHistories(List<BerkasHistory> histories) { this.histories = histories; }

    public List<BerkasAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<BerkasAttachment> attachments) { this.attachments = attachments; }

    // Helper methods
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }

    public String getStatusColor() {
        return status != null ? status.getColor() : "#999";
    }

    public String getHandlerName() {
        return currentHandler != null ? currentHandler.getNama() : "-";
    }
}
