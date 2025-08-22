package com.example.store_monitoring.repositories;

import com.example.store_monitoring.models.StoreStatus;
import com.example.store_monitoring.models.StoreStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface StoreStatusRepository extends JpaRepository<StoreStatus, StoreStatusId> {
    List<StoreStatus> findByStoreIdOrderByTimestampUtcAsc(String storeId);

    @Query("SELECT MAX(s.timestampUtc) FROM StoreStatus s")
    LocalDateTime findMaxTimestamp();
}
