package com.example.store_monitoring.controllers;

import com.example.store_monitoring.models.Report;
import com.example.store_monitoring.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
public class ReportController {
    @Autowired
    private ReportService reportService;

    @PostMapping("/trigger_report")
    public ResponseEntity<String> triggerReport() {
        String reportId = reportService.triggerReport();
        return ResponseEntity.ok(reportId);
    }

    @GetMapping("/get_report")
    public ResponseEntity<String> getReport(@RequestParam String report_id) {
        Report report = reportService.getReport(report_id);
        if ("Running".equals(report.getStatus())) {
            return ResponseEntity.ok("Running");
        } else if ("Complete".equals(report.getStatus())) {
            try {
                String csvContent = new String(Files.readAllBytes(Paths.get(report.getCsvPath())));
                return ResponseEntity.ok("Complete\n" + csvContent);
            } catch (IOException e) {
                return ResponseEntity.internalServerError().body("Error reading CSV");
            }
        }
        return ResponseEntity.notFound().build();
    }
}
