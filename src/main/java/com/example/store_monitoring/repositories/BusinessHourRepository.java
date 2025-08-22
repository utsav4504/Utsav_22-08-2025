package com.example.store_monitoring.repositories;

import com.example.store_monitoring.models.BusinessHour;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusinessHourRepository extends JpaRepository<BusinessHour, Long> {
    List<BusinessHour> findByStoreId(String storeId);
}
