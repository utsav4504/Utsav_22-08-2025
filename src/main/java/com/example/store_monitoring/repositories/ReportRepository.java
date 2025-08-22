package com.example.store_monitoring.repositories;

import com.example.store_monitoring.models.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, String> {
}
