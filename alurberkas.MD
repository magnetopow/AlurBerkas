# AlurBerkas - Specification Document
**Aplikasi Workflow Alur Berkas Pengukuran Tanah**  
**Seksi 1 Pengukuran & Pemetaan**  
**Kantor Pertanahan BPN (ATR/BPN)**  
**Versi 1.0 - Detailed Specification for AI Agent / Developer**  
**Tanggal**: 14 April 2026  
**Pembuat**: Grok (berdasarkan kebutuhan user di Pekanbaru, Riau)

## 1. Overview Proyek
**Nama Aplikasi**: AlurBerkas  
**Tujuan Utama**:  
Membuat sistem digital internal yang menggantikan Excel/WhatsApp/kertas untuk mengelola alur berkas pengukuran tanah di Seksi 1 Pengukuran.  
Proses saat ini masih manual → sering bolak-balik berkas, sulit tracking, dan lambat.

**Manfaat**:
- Real-time tracking berkas
- Mengurangi waktu proses rata-rata 30-50%
- Audit trail lengkap (siapa, kapan, apa yang dilakukan)
- Notifikasi otomatis via WhatsApp + In-app
- Siap dijadikan SaaS untuk 500+ kantor pertanahan di Indonesia

**Scope MVP (Minimum Viable Product)**:  
Hanya fokus Seksi Pengukuran (tidak termasuk Penetapan Hak / Pendaftaran Hak).

## 2. User Roles & Permissions (RBAC)

| Role              | Nama Role di App     | Hak Akses Utama                                      | Bisa Kirim ke          | Bisa Kembalikan |
|-------------------|----------------------|------------------------------------------------------|------------------------|-----------------|
| Administrasi      | admin                  | Input berkas baru, upload scan                      | Petugas Ukur           | Ya              |
| Petugas Ukur      | petugas_ukur           | Input hasil ukur lapangan, foto, koordinat          | Pemetaan               | Ya              |
| Pemetaan          | pemetaan               | Upload peta bidang, gambar situasi                  | Kasubsi 1              | Ya              |
| Kasubsi 1         | kasubsi                | Review & verifikasi lengkap                         | Kasi                   | Ya              |
| Kasi              | kasi                   | Final approval + tanda tangan digital               | Selesai / Arsip        | Ya              |
| Super Admin       | super_admin            | Semua + laporan + user management                   | -                      | -               |

Login menggunakan **NIP + Password + OTP via WhatsApp**.

## 3. Alur Bisnis Utama (Business Process Flow)

```mermaid
flowchart TD
    A[1. Administrasi<br>Terima berkas fisik dari pemohon] --> B{Cek Kelengkapan}
    B -->|Tidak lengkap| Z1[Kembalikan ke Pemohon + Alasan + Notifikasi WA]
    B -->|Lengkap| C[Input data pemohon + Generate No. Berkas otomatis<br>Upload scan dokumen]
    C --> D[Kirim ke Petugas Ukur + Notifikasi]

    D --> E[2. Petugas Ukur<br>Terima & Jadwal pengukuran lapangan]
    E --> F[Input hasil ukur: koordinat, luas, catatan<br>Upload foto patok & panorama]
    F --> G[Kirim ke Pemetaan]

    G --> H[3. Pemetaan<br>Buat peta bidang tanah]
    H --> I[Input gambar situasi + file peta (PDF/AutoCAD)<br>Catatan revisi]
    I --> J[Kirim ke Kasubsi 1]

    J --> K[4. Kasubsi 1<br>Review & Verifikasi]
    K --> L{Cek OK?}
    L -->|Tidak| N1[Kembalikan ke tahap sebelumnya + Alasan wajib]
    L -->|Ya| M[Kirim ke Kasi]

    M --> O[5. Kasi<br>Final Approval + TTD Digital]
    O --> P[Selesai → Arsip Digital<br>Notifikasi ke pemohon via Sentuh Tanahku (opsional)]

    N1 -.-> E
    N1 -.-> H
    style A fill:#e3f2fd
    style P fill:#c8e6c9