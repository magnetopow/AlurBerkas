package com.alurberkas.controller;

import com.alurberkas.model.Berkas;
import com.alurberkas.model.BerkasAttachment;
import com.alurberkas.model.BerkasHistory;
import com.alurberkas.model.User;
import com.alurberkas.model.enums.BerkasStatus;
import com.alurberkas.model.enums.Role;
import com.alurberkas.service.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/berkas")
public class BerkasController {

    private final BerkasService berkasService;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public BerkasController(BerkasService berkasService, UserService userService,
                           FileStorageService fileStorageService, NotificationService notificationService) {
        this.berkasService = berkasService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    private User getCurrentUser(Authentication auth) {
        return userService.findByNip(auth.getName());
    }

    /**
     * List semua berkas (filterable)
     */
    @GetMapping
    public String listBerkas(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String status,
                            Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Daftar Berkas");

        List<Berkas> berkasList;
        if (search != null && !search.trim().isEmpty()) {
            berkasList = berkasService.search(search);
            model.addAttribute("search", search);
        } else if (status != null && !status.isEmpty()) {
            try {
                BerkasStatus berkasStatus = BerkasStatus.valueOf(status);
                berkasList = berkasService.findByStatus(berkasStatus);
                model.addAttribute("selectedStatus", status);
            } catch (IllegalArgumentException e) {
                berkasList = berkasService.findAll();
            }
        } else {
            berkasList = berkasService.findAll();
        }

        model.addAttribute("berkasList", berkasList);
        model.addAttribute("statuses", BerkasStatus.values());
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));

        return "berkas/list";
    }

    /**
     * Form input berkas baru (Admin only)
     */
    @GetMapping("/create")
    public String createForm(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Input Berkas Baru");
        model.addAttribute("berkas", new Berkas());
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "berkas/create";
    }

    /**
     * Process berkas baru
     */
    @PostMapping("/create")
    public String createBerkas(@ModelAttribute Berkas berkas,
                              @RequestParam(value = "files", required = false) MultipartFile[] files,
                              Authentication auth, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(auth);

        try {
            Berkas saved = berkasService.createBerkas(berkas, user);

            // Handle file uploads
            if (files != null) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        fileStorageService.storeFile(file, saved, user, BerkasStatus.DITERIMA, "Dokumen pemohon");
                    }
                }
            }

            redirectAttributes.addFlashAttribute("success",
                    "Berkas berhasil dibuat dengan nomor: " + saved.getNoBerkas());
            return "redirect:/berkas/" + saved.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal membuat berkas: " + e.getMessage());
            return "redirect:/berkas/create";
        }
    }

    /**
     * Detail berkas
     */
    @GetMapping("/{id}")
    public String detailBerkas(@PathVariable Long id, Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        Berkas berkas = berkasService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Berkas tidak ditemukan: " + id));

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Detail Berkas " + berkas.getNoBerkas());
        model.addAttribute("berkas", berkas);

        // History
        List<BerkasHistory> histories = berkasService.getHistory(id);
        model.addAttribute("histories", histories);

        // Attachments
        List<BerkasAttachment> attachments = fileStorageService.getAttachments(id);
        model.addAttribute("attachments", attachments);

        // Can this user take action on this berkas?
        boolean canForward = canForward(user, berkas);
        boolean canReturn = canReturn(user, berkas);
        boolean canEdit = canEdit(user, berkas);
        model.addAttribute("canForward", canForward);
        model.addAttribute("canReturn", canReturn);
        model.addAttribute("canEdit", canEdit);

        // Get possible receivers for forwarding
        if (canForward) {
            BerkasStatus nextStatus = berkas.getStatus().getNextStatus();
            if (nextStatus != null && nextStatus.getHandlerRole() != null) {
                List<User> receivers = userService.findByRole(nextStatus.getHandlerRole());
                model.addAttribute("receivers", receivers);
            }
            model.addAttribute("nextStatus", berkas.getStatus().getNextStatus());
        }

        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        model.addAttribute("statuses", BerkasStatus.values());

        return "berkas/detail";
    }

    /**
     * Kirim berkas ke tahap berikutnya
     */
    @PostMapping("/{id}/kirim")
    public String kirimBerkas(@PathVariable Long id,
                             @RequestParam(required = false) Long receiverId,
                             @RequestParam(required = false) String keterangan,
                             Authentication auth, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(auth);

        try {
            User receiver = null;
            if (receiverId != null) {
                receiver = userService.findById(receiverId).orElse(null);
            }

            berkasService.kirimBerkas(id, user, receiver, keterangan);
            redirectAttributes.addFlashAttribute("success", "Berkas berhasil dikirim ke tahap berikutnya");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengirim berkas: " + e.getMessage());
        }

        return "redirect:/berkas/" + id;
    }

    /**
     * Kembalikan berkas
     */
    @PostMapping("/{id}/kembalikan")
    public String kembalikanBerkas(@PathVariable Long id,
                                  @RequestParam String alasan,
                                  @RequestParam(required = false) String targetStatus,
                                  Authentication auth, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(auth);

        try {
            BerkasStatus target = null;
            if (targetStatus != null && !targetStatus.isEmpty()) {
                target = BerkasStatus.valueOf(targetStatus);
            }

            berkasService.kembalikanBerkas(id, user, alasan, target);
            redirectAttributes.addFlashAttribute("success", "Berkas berhasil dikembalikan");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengembalikan berkas: " + e.getMessage());
        }

        return "redirect:/berkas/" + id;
    }

    /**
     * Resubmit berkas yang dikembalikan
     */
    @PostMapping("/{id}/resubmit")
    public String resubmitBerkas(@PathVariable Long id,
                                @RequestParam String keterangan,
                                Authentication auth, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(auth);

        try {
            Berkas berkas = berkasService.findById(id).orElseThrow();
            // Determine which status to resubmit to based on user role
            BerkasStatus targetStatus = switch (user.getRole()) {
                case ADMIN -> BerkasStatus.DITERIMA;
                case PETUGAS_UKUR -> BerkasStatus.PENGUKURAN;
                case PEMETAAN -> BerkasStatus.PEMETAAN;
                default -> BerkasStatus.DITERIMA;
            };

            berkasService.resubmitBerkas(id, user, targetStatus, keterangan);
            redirectAttributes.addFlashAttribute("success", "Berkas berhasil diajukan ulang");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengajukan ulang: " + e.getMessage());
        }

        return "redirect:/berkas/" + id;
    }

    /**
     * Update data berkas (measurement, mapping, etc.)
     */
    @PostMapping("/{id}/update")
    public String updateBerkas(@PathVariable Long id,
                              @RequestParam(required = false) String koordinatTanah,
                              @RequestParam(required = false) Double hasilLuasUkur,
                              @RequestParam(required = false) String catatanUkur,
                              @RequestParam(required = false) String tanggalUkur,
                              @RequestParam(required = false) String catatanPeta,
                              @RequestParam(required = false) String noPetaBidang,
                              @RequestParam(required = false) String catatan,
                              @RequestParam(value = "files", required = false) MultipartFile[] files,
                              Authentication auth, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(auth);

        try {
            Berkas berkas = berkasService.findById(id).orElseThrow();

            // Update fields based on what's provided
            if (koordinatTanah != null) berkas.setKoordinatTanah(koordinatTanah);
            if (hasilLuasUkur != null) berkas.setHasilLuasUkur(hasilLuasUkur);
            if (catatanUkur != null) berkas.setCatatanUkur(catatanUkur);
            if (tanggalUkur != null && !tanggalUkur.isEmpty()) {
                berkas.setTanggalUkur(LocalDate.parse(tanggalUkur));
            }
            if (catatanPeta != null) berkas.setCatatanPeta(catatanPeta);
            if (noPetaBidang != null) berkas.setNoPetaBidang(noPetaBidang);
            if (catatan != null) berkas.setCatatan(catatan);

            berkasService.updateBerkas(berkas);

            // Handle file uploads
            if (files != null) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        fileStorageService.storeFile(file, berkas, user, berkas.getStatus(), "Dokumen " + berkas.getStatus().getDisplayName());
                    }
                }
            }

            redirectAttributes.addFlashAttribute("success", "Data berkas berhasil diperbarui");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui: " + e.getMessage());
        }

        return "redirect:/berkas/" + id;
    }

    /**
     * Download attachment
     */
    @GetMapping("/attachment/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        try {
            BerkasAttachment attachment = fileStorageService.getAttachments(0L).stream()
                    .filter(a -> a.getId().equals(attachmentId))
                    .findFirst()
                    .orElse(null);

            // Fallback: query directly
            Path filePath = fileStorageService.getFilePath(
                    attachment != null ? attachment.getStoredFileName() : attachmentId.toString());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + (attachment != null ? attachment.getFileName() : "file") + "\"")
                        .body(resource);
            }
        } catch (Exception e) {
            // Fall through
        }

        return ResponseEntity.notFound().build();
    }

    // Permission helpers
    private boolean canForward(User user, Berkas berkas) {
        if (user.getRole() == Role.SUPER_ADMIN) return true;
        BerkasStatus status = berkas.getStatus();
        return switch (user.getRole()) {
            case ADMIN -> status == BerkasStatus.DITERIMA;
            case PETUGAS_UKUR -> status == BerkasStatus.PENGUKURAN;
            case PEMETAAN -> status == BerkasStatus.PEMETAAN;
            case KASUBSI -> status == BerkasStatus.VERIFIKASI;
            case KASI -> status == BerkasStatus.APPROVAL;
            default -> false;
        };
    }

    private boolean canReturn(User user, Berkas berkas) {
        if (user.getRole() == Role.SUPER_ADMIN) return true;
        BerkasStatus status = berkas.getStatus();
        return switch (user.getRole()) {
            case PETUGAS_UKUR -> status == BerkasStatus.PENGUKURAN;
            case PEMETAAN -> status == BerkasStatus.PEMETAAN;
            case KASUBSI -> status == BerkasStatus.VERIFIKASI;
            case KASI -> status == BerkasStatus.APPROVAL;
            default -> false;
        };
    }

    private boolean canEdit(User user, Berkas berkas) {
        if (user.getRole() == Role.SUPER_ADMIN) return true;
        BerkasStatus status = berkas.getStatus();
        return switch (user.getRole()) {
            case ADMIN -> status == BerkasStatus.DITERIMA || status == BerkasStatus.DIKEMBALIKAN;
            case PETUGAS_UKUR -> status == BerkasStatus.PENGUKURAN || status == BerkasStatus.DIKEMBALIKAN;
            case PEMETAAN -> status == BerkasStatus.PEMETAAN || status == BerkasStatus.DIKEMBALIKAN;
            case KASUBSI -> status == BerkasStatus.VERIFIKASI;
            case KASI -> status == BerkasStatus.APPROVAL;
            default -> false;
        };
    }
}
