package com.alurberkas.config;

import com.alurberkas.model.Berkas;
import com.alurberkas.model.User;
import com.alurberkas.model.enums.BerkasStatus;
import com.alurberkas.model.enums.Role;
import com.alurberkas.repository.UserRepository;
import com.alurberkas.service.BerkasService;
import com.alurberkas.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    private final BerkasService berkasService;

    public DataSeeder(UserService userService, UserRepository userRepository, BerkasService berkasService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.berkasService = berkasService;
    }

    @Override
    public void run(String... args) {
        // Only seed if no users exist
        if (userRepository.count() > 0) {
            return;
        }

        System.out.println("=== Seeding Database ===");

        // Create users for each role
        User admin = userService.createUser("198501012010011001", "Siti Nurhaliza", "admin123", Role.ADMIN, "081234567890");
        User petugasUkur1 = userService.createUser("198602022011012002", "Ahmad Fauzi", "ukur123", Role.PETUGAS_UKUR, "081234567891");
        User petugasUkur2 = userService.createUser("198703032012013003", "Budi Santoso", "ukur123", Role.PETUGAS_UKUR, "081234567892");
        User pemetaan = userService.createUser("198804042013014004", "Diana Putri", "peta123", Role.PEMETAAN, "081234567893");
        User kasubsi = userService.createUser("198505052009015005", "Hendri Wijaya", "kasubsi123", Role.KASUBSI, "081234567894");
        User kasi = userService.createUser("198206062008016006", "Ir. Rahman Hakim", "kasi123", Role.KASI, "081234567895");
        User superAdmin = userService.createUser("superadmin", "Administrator Sistem", "super123", Role.SUPER_ADMIN, "081234567899");

        System.out.println("✓ Users created");

        // Create sample berkas at various stages
        // Berkas 1: Diterima (fresh)
        Berkas b1 = new Berkas();
        b1.setNamaPemohon("Joko Widodo");
        b1.setNikPemohon("1471012345678901");
        b1.setNoHpPemohon("085612345678");
        b1.setAlamatPemohon("Jl. Sudirman No. 10, Pekanbaru");
        b1.setAlamatTanah("Jl. Harapan Raya, Kel. Tangkerang Tengah");
        b1.setKelurahan("Tangkerang Tengah");
        b1.setKecamatan("Marpoyan Damai");
        b1.setLuasTanah(500.0);
        b1.setJenisPermohonan("Pengukuran Pertama");
        berkasService.createBerkas(b1, admin);

        // Berkas 2: In pengukuran
        Berkas b2 = new Berkas();
        b2.setNamaPemohon("Sari Dewi");
        b2.setNikPemohon("1471012345678902");
        b2.setNoHpPemohon("085612345679");
        b2.setAlamatPemohon("Jl. Nangka No. 5, Pekanbaru");
        b2.setAlamatTanah("Jl. Garuda Sakti KM 3, Kel. Simpang Baru");
        b2.setKelurahan("Simpang Baru");
        b2.setKecamatan("Tampan");
        b2.setLuasTanah(300.0);
        b2.setJenisPermohonan("Pengukuran Pertama");
        Berkas saved2 = berkasService.createBerkas(b2, admin);
        berkasService.kirimBerkas(saved2.getId(), admin, petugasUkur1, "Berkas lengkap, silakan diukur");

        // Berkas 3: In pemetaan
        Berkas b3 = new Berkas();
        b3.setNamaPemohon("Andi Prasetyo");
        b3.setNikPemohon("1471012345678903");
        b3.setNoHpPemohon("085612345680");
        b3.setAlamatPemohon("Jl. Riau No. 15, Pekanbaru");
        b3.setAlamatTanah("Jl. HR. Soebrantas KM 12, Kel. Panam");
        b3.setKelurahan("Tuah Karya");
        b3.setKecamatan("Tampan");
        b3.setLuasTanah(750.0);
        b3.setJenisPermohonan("Pengembalian Batas");
        Berkas saved3 = berkasService.createBerkas(b3, admin);
        berkasService.kirimBerkas(saved3.getId(), admin, petugasUkur1, "Berkas siap ukur");
        // Simulate petugas ukur finishing
        saved3 = berkasService.findById(saved3.getId()).get();
        saved3.setKoordinatTanah("0.5071° N, 101.4478° E");
        saved3.setHasilLuasUkur(748.5);
        saved3.setCatatanUkur("Tanah berbatasan dengan sungai di sisi utara");
        berkasService.updateBerkas(saved3);
        berkasService.kirimBerkas(saved3.getId(), petugasUkur1, pemetaan, "Hasil ukur selesai");

        // Berkas 4: In verifikasi
        Berkas b4 = new Berkas();
        b4.setNamaPemohon("Maria Ulfa");
        b4.setNikPemohon("1471012345678904");
        b4.setNoHpPemohon("085612345681");
        b4.setAlamatPemohon("Jl. Durian No. 8, Pekanbaru");
        b4.setAlamatTanah("Jl. Soekarno-Hatta KM 5, Kel. Maharatu");
        b4.setKelurahan("Maharatu");
        b4.setKecamatan("Marpoyan Damai");
        b4.setLuasTanah(400.0);
        b4.setJenisPermohonan("Pengukuran Pertama");
        Berkas saved4 = berkasService.createBerkas(b4, admin);
        berkasService.kirimBerkas(saved4.getId(), admin, petugasUkur2, "Silakan diukur");
        berkasService.kirimBerkas(saved4.getId(), petugasUkur2, pemetaan, "Pengukuran selesai");
        berkasService.kirimBerkas(saved4.getId(), pemetaan, kasubsi, "Peta bidang selesai");

        // Berkas 5: In approval
        Berkas b5 = new Berkas();
        b5.setNamaPemohon("Rizky Ramadhan");
        b5.setNikPemohon("1471012345678905");
        b5.setNoHpPemohon("085612345682");
        b5.setAlamatPemohon("Jl. Melati No. 20, Pekanbaru");
        b5.setAlamatTanah("Jl. Arifin Ahmad No. 77, Kel. Sidomulyo Timur");
        b5.setKelurahan("Sidomulyo Timur");
        b5.setKecamatan("Marpoyan Damai");
        b5.setLuasTanah(250.0);
        b5.setJenisPermohonan("Pengukuran Pertama");
        Berkas saved5 = berkasService.createBerkas(b5, admin);
        berkasService.kirimBerkas(saved5.getId(), admin, petugasUkur1, "Siap ukur");
        berkasService.kirimBerkas(saved5.getId(), petugasUkur1, pemetaan, "Ukur selesai");
        berkasService.kirimBerkas(saved5.getId(), pemetaan, kasubsi, "Peta selesai");
        berkasService.kirimBerkas(saved5.getId(), kasubsi, kasi, "Verified, siap approval");

        // Berkas 6: Selesai
        Berkas b6 = new Berkas();
        b6.setNamaPemohon("Putri Handayani");
        b6.setNikPemohon("1471012345678906");
        b6.setNoHpPemohon("085612345683");
        b6.setAlamatPemohon("Jl. Anggrek No. 3, Pekanbaru");
        b6.setAlamatTanah("Jl. Paus No. 15, Kel. Tangkerang Selatan");
        b6.setKelurahan("Tangkerang Selatan");
        b6.setKecamatan("Bukit Raya");
        b6.setLuasTanah(600.0);
        b6.setJenisPermohonan("Pengembalian Batas");
        Berkas saved6 = berkasService.createBerkas(b6, admin);
        berkasService.kirimBerkas(saved6.getId(), admin, petugasUkur2, "Siap ukur");
        berkasService.kirimBerkas(saved6.getId(), petugasUkur2, pemetaan, "Ukur selesai");
        berkasService.kirimBerkas(saved6.getId(), pemetaan, kasubsi, "Peta selesai");
        berkasService.kirimBerkas(saved6.getId(), kasubsi, kasi, "Verified");
        berkasService.kirimBerkas(saved6.getId(), kasi, null, "Approved & tanda tangan digital");

        // Berkas 7: Dikembalikan
        Berkas b7 = new Berkas();
        b7.setNamaPemohon("Bambang Supriadi");
        b7.setNikPemohon("1471012345678907");
        b7.setNoHpPemohon("085612345684");
        b7.setAlamatPemohon("Jl. Dahlia No. 12, Pekanbaru");
        b7.setAlamatTanah("Jl. Taskurun No. 30, Kel. Wonorejo");
        b7.setKelurahan("Wonorejo");
        b7.setKecamatan("Marpoyan Damai");
        b7.setLuasTanah(350.0);
        b7.setJenisPermohonan("Pengukuran Pertama");
        Berkas saved7 = berkasService.createBerkas(b7, admin);
        berkasService.kirimBerkas(saved7.getId(), admin, petugasUkur1, "Siap ukur");
        berkasService.kirimBerkas(saved7.getId(), petugasUkur1, pemetaan, "Ukur selesai");
        berkasService.kembalikanBerkas(saved7.getId(), pemetaan, "Koordinat tidak lengkap, mohon dilengkapi", BerkasStatus.PENGUKURAN);

        System.out.println("✓ Sample berkas created");
        System.out.println("=== Seeding Complete ===");
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║          AKUN LOGIN SAMPLE                  ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║ Admin:         NIP: 198501012010011001      ║");
        System.out.println("║                Pass: admin123               ║");
        System.out.println("║ Petugas Ukur:  NIP: 198602022011012002      ║");
        System.out.println("║                Pass: ukur123                ║");
        System.out.println("║ Pemetaan:      NIP: 198804042013014004      ║");
        System.out.println("║                Pass: peta123                ║");
        System.out.println("║ Kasubsi:       NIP: 198505052009015005      ║");
        System.out.println("║                Pass: kasubsi123             ║");
        System.out.println("║ Kasi:          NIP: 198206062008016006      ║");
        System.out.println("║                Pass: kasi123                ║");
        System.out.println("║ Super Admin:   NIP: superadmin              ║");
        System.out.println("║                Pass: super123               ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }
}
