package com.alurberkas.service;

import com.alurberkas.model.*;
import com.alurberkas.model.enums.BerkasStatus;
import com.alurberkas.model.enums.Role;
import com.alurberkas.repository.BerkasRepository;
import com.alurberkas.repository.BerkasHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BerkasService {

    private final BerkasRepository berkasRepository;
    private final BerkasHistoryRepository historyRepository;
    private final NotificationService notificationService;

    public BerkasService(BerkasRepository berkasRepository,
                         BerkasHistoryRepository historyRepository,
                         NotificationService notificationService) {
        this.berkasRepository = berkasRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
    }

    /**
     * Generate nomor berkas otomatis: BRK-YYYYMMDD-XXXX
     */
    private String generateNoBerkas() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = berkasRepository.count() + 1;
        return String.format("BRK-%s-%04d", dateStr, count);
    }

    /**
     * Admin membuat berkas baru
     */
    @Transactional
    public Berkas createBerkas(Berkas berkas, User admin) {
        berkas.setNoBerkas(generateNoBerkas());
        berkas.setStatus(BerkasStatus.DITERIMA);
        berkas.setCreatedBy(admin);
        berkas.setCurrentHandler(admin);

        Berkas saved = berkasRepository.save(berkas);

        // Record history
        BerkasHistory history = new BerkasHistory(
                saved, null, BerkasStatus.DITERIMA,
                admin, "CREATE", "Berkas baru diterima dari pemohon: " + berkas.getNamaPemohon()
        );
        historyRepository.save(history);

        return saved;
    }

    /**
     * Kirim berkas ke tahap berikutnya
     */
    @Transactional
    public Berkas kirimBerkas(Long berkasId, User sender, User receiver, String keterangan) {
        Berkas berkas = berkasRepository.findById(berkasId)
                .orElseThrow(() -> new IllegalArgumentException("Berkas tidak ditemukan: " + berkasId));

        BerkasStatus currentStatus = berkas.getStatus();
        BerkasStatus nextStatus = currentStatus.getNextStatus();

        if (nextStatus == null) {
            throw new IllegalStateException("Berkas sudah di tahap akhir");
        }

        BerkasStatus fromStatus = berkas.getStatus();
        berkas.setStatus(nextStatus);
        berkas.setCurrentHandler(receiver);
        berkas.setAlasanPengembalian(null);

        if (nextStatus == BerkasStatus.SELESAI) {
            berkas.setCompletedAt(LocalDateTime.now());
        }

        Berkas saved = berkasRepository.save(berkas);

        // Record history
        BerkasHistory history = new BerkasHistory(
                saved, fromStatus, nextStatus,
                sender, "KIRIM",
                keterangan != null ? keterangan : "Berkas dikirim ke " + nextStatus.getDisplayName()
        );
        historyRepository.save(history);

        // Notify receiver
        if (receiver != null) {
            notificationService.createNotification(
                    receiver, saved,
                    "Berkas Masuk: " + saved.getNoBerkas(),
                    "Anda menerima berkas " + saved.getNoBerkas() + " (" + saved.getNamaPemohon() + ") " +
                            "untuk proses " + nextStatus.getDisplayName(),
                    "INFO"
            );
        }

        return saved;
    }

    /**
     * Kembalikan berkas ke tahap sebelumnya
     */
    @Transactional
    public Berkas kembalikanBerkas(Long berkasId, User sender, String alasan, BerkasStatus targetStatus) {
        Berkas berkas = berkasRepository.findById(berkasId)
                .orElseThrow(() -> new IllegalArgumentException("Berkas tidak ditemukan: " + berkasId));

        if (alasan == null || alasan.trim().isEmpty()) {
            throw new IllegalArgumentException("Alasan pengembalian wajib diisi");
        }

        BerkasStatus fromStatus = berkas.getStatus();

        // If target status not specified, go to previous
        if (targetStatus == null) {
            targetStatus = fromStatus.getPreviousStatus();
        }

        if (targetStatus == null) {
            throw new IllegalStateException("Berkas tidak bisa dikembalikan lagi");
        }

        berkas.setStatus(BerkasStatus.DIKEMBALIKAN);
        berkas.setAlasanPengembalian(alasan);

        // Find a handler for the target status
        Role targetRole = targetStatus.getHandlerRole();
        // Keep currentHandler for now, will be updated when re-processed

        Berkas saved = berkasRepository.save(berkas);

        // Record history
        BerkasHistory history = new BerkasHistory(
                saved, fromStatus, BerkasStatus.DIKEMBALIKAN,
                sender, "KEMBALIKAN",
                "Dikembalikan dari " + fromStatus.getDisplayName() + ": " + alasan
        );
        historyRepository.save(history);

        // Notify about return
        if (berkas.getCreatedBy() != null) {
            notificationService.createNotification(
                    berkas.getCreatedBy(), saved,
                    "Berkas Dikembalikan: " + saved.getNoBerkas(),
                    "Berkas " + saved.getNoBerkas() + " dikembalikan dari tahap " +
                            fromStatus.getDisplayName() + ". Alasan: " + alasan,
                    "WARNING"
            );
        }

        return saved;
    }

    /**
     * Resubmit berkas yang sudah dikembalikan
     */
    @Transactional
    public Berkas resubmitBerkas(Long berkasId, User sender, BerkasStatus targetStatus, String keterangan) {
        Berkas berkas = berkasRepository.findById(berkasId)
                .orElseThrow(() -> new IllegalArgumentException("Berkas tidak ditemukan: " + berkasId));

        if (berkas.getStatus() != BerkasStatus.DIKEMBALIKAN) {
            throw new IllegalStateException("Berkas tidak dalam status dikembalikan");
        }

        berkas.setStatus(targetStatus);
        berkas.setCurrentHandler(sender);
        berkas.setAlasanPengembalian(null);

        Berkas saved = berkasRepository.save(berkas);

        BerkasHistory history = new BerkasHistory(
                saved, BerkasStatus.DIKEMBALIKAN, targetStatus,
                sender, "RESUBMIT",
                keterangan != null ? keterangan : "Berkas diajukan ulang ke " + targetStatus.getDisplayName()
        );
        historyRepository.save(history);

        return saved;
    }

    /**
     * Update data berkas (sesuai tahap)
     */
    @Transactional
    public Berkas updateBerkas(Berkas berkas) {
        return berkasRepository.save(berkas);
    }

    // Query methods
    public Optional<Berkas> findById(Long id) {
        return berkasRepository.findById(id);
    }

    public List<Berkas> findAll() {
        return berkasRepository.findAllOrderByUpdatedAtDesc();
    }

    public List<Berkas> findByStatus(BerkasStatus status) {
        return berkasRepository.findByStatusOrderByUpdatedAtDesc(status);
    }

    public List<Berkas> findByHandler(User handler) {
        return berkasRepository.findByCurrentHandlerOrderByUpdatedAtDesc(handler);
    }

    public List<Berkas> findByCreator(User creator) {
        return berkasRepository.findByCreatedByOrderByCreatedAtDesc(creator);
    }

    public List<Berkas> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return berkasRepository.searchBerkas(keyword.trim());
    }

    /**
     * Get berkas relevant to a specific role
     */
    public List<Berkas> findByRole(User user) {
        Role role = user.getRole();
        return switch (role) {
            case ADMIN -> berkasRepository.findByStatusOrderByUpdatedAtDesc(BerkasStatus.DITERIMA);
            case PETUGAS_UKUR -> berkasRepository.findByStatusAndHandler(BerkasStatus.PENGUKURAN, user);
            case PEMETAAN -> berkasRepository.findByStatusAndHandler(BerkasStatus.PEMETAAN, user);
            case KASUBSI -> berkasRepository.findByStatusOrderByUpdatedAtDesc(BerkasStatus.VERIFIKASI);
            case KASI -> berkasRepository.findByStatusOrderByUpdatedAtDesc(BerkasStatus.APPROVAL);
            case SUPER_ADMIN -> findAll();
        };
    }

    // Statistics
    public long countAll() {
        return berkasRepository.count();
    }

    public long countByStatus(BerkasStatus status) {
        return berkasRepository.countByStatus(status);
    }

    public long countCompleted() {
        return berkasRepository.countCompleted();
    }

    public long countInProgress() {
        return berkasRepository.countInProgress();
    }

    public Map<BerkasStatus, Long> getStatusCounts() {
        Map<BerkasStatus, Long> counts = new LinkedHashMap<>();
        for (BerkasStatus status : BerkasStatus.values()) {
            counts.put(status, berkasRepository.countByStatus(status));
        }
        return counts;
    }

    public List<BerkasHistory> getHistory(Long berkasId) {
        return historyRepository.findByBerkasIdOrderByTimestampAsc(berkasId);
    }
}
