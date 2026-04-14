package com.alurberkas.repository;

import com.alurberkas.model.Berkas;
import com.alurberkas.model.User;
import com.alurberkas.model.enums.BerkasStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BerkasRepository extends JpaRepository<Berkas, Long> {

    Optional<Berkas> findByNoBerkas(String noBerkas);

    List<Berkas> findByStatus(BerkasStatus status);

    List<Berkas> findByCurrentHandler(User handler);

    List<Berkas> findByCurrentHandlerOrderByUpdatedAtDesc(User handler);

    List<Berkas> findByStatusOrderByUpdatedAtDesc(BerkasStatus status);

    List<Berkas> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    @Query("SELECT b FROM Berkas b WHERE b.status = :status AND b.currentHandler = :handler ORDER BY b.updatedAt DESC")
    List<Berkas> findByStatusAndHandler(@Param("status") BerkasStatus status, @Param("handler") User handler);

    @Query("SELECT b FROM Berkas b WHERE " +
           "LOWER(b.noBerkas) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.namaPemohon) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.alamatTanah) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY b.updatedAt DESC")
    List<Berkas> searchBerkas(@Param("keyword") String keyword);

    long countByStatus(BerkasStatus status);

    @Query("SELECT b.status, COUNT(b) FROM Berkas b GROUP BY b.status")
    List<Object[]> countByStatusGroup();

    @Query("SELECT COUNT(b) FROM Berkas b WHERE b.currentHandler = :handler")
    long countByHandler(@Param("handler") User handler);

    @Query("SELECT b FROM Berkas b ORDER BY b.updatedAt DESC")
    List<Berkas> findAllOrderByUpdatedAtDesc();

    @Query("SELECT COUNT(b) FROM Berkas b WHERE b.status = 'SELESAI'")
    long countCompleted();

    @Query("SELECT COUNT(b) FROM Berkas b WHERE b.status != 'SELESAI'")
    long countInProgress();
}
