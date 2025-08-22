package com.example.store_monitoring.services;

import com.example.store_monitoring.models.*;
import com.example.store_monitoring.repositories.*;
import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DataIngestionService {
    @Autowired
    private  StoreStatusRepository statusRepo;
    @Autowired
    private  BusinessHourRepository hourRepo;
    @Autowired
    private TimezoneRepository timezoneRepo;

    @PostConstruct
    public void ingestData() {
        // Skip ingestion if store status table already has data
        if (statusRepo.count() > 0) {
            System.out.println("Data already ingested; skipping ingestion.");
            return;
        }

        // Ingest timezones (default America/Chicago if missing)
        Map<String, String> timezones = new HashMap<>();
        try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/data/timezones.csv"))) {
            reader.readNext(); // Skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length >= 2) {
                    timezones.put(line[0], line[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        timezones.forEach((storeId, tz) -> {
            if (!timezoneRepo.existsById(storeId)) {
                Timezone timezone = new Timezone();
                timezone.setStoreId(storeId);
                timezone.setTimezoneStr(tz != null ? tz : "America/Chicago");
                timezoneRepo.save(timezone);
            }
        });

        // Ingest business hours (skip if storeId + dayOfWeek exists)
        try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/data/menu_hours.csv"))) {
            reader.readNext(); // Skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length >= 4) {
                    String storeId = line[0];
                    int dayOfWeek = Integer.parseInt(line[1]);
                    boolean exists = hourRepo.findByStoreId(storeId).stream()
                            .anyMatch(h -> h.getDayOfWeek() == dayOfWeek);
                    if (!exists) {
                        BusinessHour hour = new BusinessHour();
                        hour.setStoreId(storeId);
                        hour.setDayOfWeek(dayOfWeek);
                        hour.setStartTimeLocal(LocalTime.parse(line[2]));
                        hour.setEndTimeLocal(LocalTime.parse(line[3]));
                        hourRepo.save(hour);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ingest statuses (skip if StoreStatus with composite key exists)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS z");
        try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/data/store_status.csv"))) {
            reader.readNext(); // Skip header
            String[] line;
            int skipped = 0;
            int inserted = 0;
            while ((line = reader.readNext()) != null) {
                if (line.length >= 3) {
                    String storeId = line[0].trim();
                    ZonedDateTime parsedZoned;
                    try {
                        parsedZoned = ZonedDateTime.parse(line[2].trim(), formatter);
                    } catch (Exception ex) {
                        System.err.println("Skipping invalid status row (bad date): " + Arrays.toString(line));
                        skipped++;
                        continue;
                    }
                    LocalDateTime timestampUtc = parsedZoned.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
                    StoreStatusId statusId = new StoreStatusId();
                    statusId.setStoreId(storeId);
                    statusId.setTimestampUtc(timestampUtc);
                    boolean exists = statusRepo.existsById(statusId);

                    if (!exists) {
                        StoreStatus status = new StoreStatus();
                        status.setStoreId(storeId);
                        status.setTimestampUtc(timestampUtc);
                        status.setStatus(line[1].trim());
                        statusRepo.save(status);
                        inserted++;
                    } else {
                        skipped++;
                    }
                }
            }
            System.out.println("Status ingestion complete. Inserted rows: " + inserted + " | Skipped rows: " + skipped);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
