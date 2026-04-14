package com.alurberkas.repository;

import com.alurberkas.model.BerkasAttachment;
import com.alurberkas.model.enums.BerkasStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BerkasAttachmentRepository extends JpaRepository<BerkasAttachment, Long> {

    List<BerkasAttachment> findByBerkasIdOrderByUploadedAtDesc(Long berkasId);

    List<BerkasAttachment> findByBerkasIdAndStage(Long berkasId, BerkasStatus stage);

    long countByBerkasId(Long berkasId);
}
