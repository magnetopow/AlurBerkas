package com.alurberkas.repository;

import com.alurberkas.model.BerkasHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BerkasHistoryRepository extends JpaRepository<BerkasHistory, Long> {

    List<BerkasHistory> findByBerkasIdOrderByTimestampDesc(Long berkasId);

    List<BerkasHistory> findByBerkasIdOrderByTimestampAsc(Long berkasId);

    List<BerkasHistory> findByActorIdOrderByTimestampDesc(Long actorId);
}
