package com.example.store_monitoring.repositories;

import com.example.store_monitoring.models.Timezone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimezoneRepository extends JpaRepository<Timezone, String> {
}
