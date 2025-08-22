package com.example.store_monitoring.services;

import com.example.store_monitoring.models.*;
import com.example.store_monitoring.repositories.*;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    @Autowired
    private  StoreStatusRepository statusRepo;
    @Autowired
    private  BusinessHourRepository hourRepo;
    @Autowired
    private  TimezoneRepository timezoneRepo;
    @Autowired
    private  ReportRepository reportRepo;

    public String triggerReport() {
        String reportId = UUID.randomUUID().toString();
        Report report = new Report();
        report.setReportId(reportId);
        report.setStatus("Running");
        reportRepo.save(report);

        // Run in background
        new Thread(() -> generateReport(reportId)).start();
        return reportId;
    }

    private void generateReport(String reportId) {
        LocalDateTime current = statusRepo.findMaxTimestamp(); // Hardcoded max timestamp
        List<String> storeIds = statusRepo.findAll().stream().map(StoreStatus::getStoreId).distinct().collect(Collectors.toList());

        String csvPath = "reports/" + reportId + ".csv";
        new File("reports").mkdirs();

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvPath))) {
            writer.writeNext(new String[]{"store_id", "uptime_last_hour", "uptime_last_day", "uptime_last_week", "downtime_last_hour", "downtime_last_day", "downtime_last_week"});

            for (String storeId : storeIds) {
                List<BusinessHour> hours = hourRepo.findByStoreId(storeId);
                if (hours.isEmpty()) {

                    for (int day = 0; day < 7; day++) {
                        BusinessHour defaultHour = new BusinessHour();
                        defaultHour.setDayOfWeek(day);
                        defaultHour.setStartTimeLocal(LocalTime.of(0, 0));
                        defaultHour.setEndTimeLocal(LocalTime.of(23, 59));
                        hours.add(defaultHour);
                    }
                }
                Timezone tz = timezoneRepo.findById(storeId).orElse(new Timezone());
                if (tz.getTimezoneStr() == null) tz.setTimezoneStr("America/Chicago");
                ZoneId zone = ZoneId.of(tz.getTimezoneStr());

                List<StoreStatus> polls = statusRepo.findByStoreIdOrderByTimestampUtcAsc(storeId);


                long[] lastHour = calculateUptimeDowntime(polls, hours, zone, current.minusHours(1), current);
                long[] lastDay = calculateUptimeDowntime(polls, hours, zone, current.minusDays(1), current);
                long[] lastWeek = calculateUptimeDowntime(polls, hours, zone, current.minusWeeks(1), current);

                writer.writeNext(new String[]{
                        storeId,
                        String.valueOf(lastHour[0]), // uptime minutes
                        String.valueOf(lastDay[0] / 60), // hours
                        String.valueOf(lastWeek[0] / 60),
                        String.valueOf(lastHour[1]),
                        String.valueOf(lastDay[1] / 60),
                        String.valueOf(lastWeek[1] / 60)
                });
            }

            Report report = reportRepo.findById(reportId).get();
            report.setStatus("Complete");
            report.setCsvPath(csvPath);
            reportRepo.save(report);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private long[] calculateUptimeDowntime(List<StoreStatus> polls, List<BusinessHour> hours, ZoneId zone, LocalDateTime start, LocalDateTime end) {
        long uptime = 0;
        long downtime = 0;


        List<Interval> businessWindows = getBusinessWindows(hours, zone, start, end);


        List<StoreStatus> relevantPolls = polls.stream()
                .filter(p -> p.getTimestampUtc().isAfter(start) && p.getTimestampUtc().isBefore(end))
                .sorted(Comparator.comparing(StoreStatus::getTimestampUtc))
                .collect(Collectors.toList());

        if (relevantPolls.isEmpty()) {

            for (Interval window : businessWindows) {
                downtime += ChronoUnit.MINUTES.between(window.start, window.end);
            }
            return new long[]{uptime, downtime};
        }


        LocalDateTime prevTime = start;
        String prevStatus = "inactive";

        for (StoreStatus poll : relevantPolls) {
            LocalDateTime currTime = poll.getTimestampUtc();
            String currStatus = poll.getStatus();


            long segmentMinutes = calculateOverlapMinutes(businessWindows, prevTime, currTime, prevStatus);
            if (prevStatus.equals("active")) uptime += segmentMinutes;
            else downtime += segmentMinutes;

            prevTime = currTime;
            prevStatus = currStatus;
        }


        long lastMinutes = calculateOverlapMinutes(businessWindows, prevTime, end, prevStatus);
        if (prevStatus.equals("active")) uptime += lastMinutes;
        else downtime += lastMinutes;

        return new long[]{uptime, downtime};
    }

    private List<Interval> getBusinessWindows(List<BusinessHour> hours, ZoneId zone, LocalDateTime startUtc, LocalDateTime endUtc) {
        List<Interval> windows = new ArrayList<>();
        LocalDateTime current = startUtc;
        while (current.isBefore(endUtc)) {
            int dayOfWeek = current.getDayOfWeek().getValue() % 7; // 1=Monday, but problem uses 0=Monday
            BusinessHour hour = hours.stream().filter(h -> h.getDayOfWeek() == dayOfWeek).findFirst().orElse(null);
            if (hour != null) {
                LocalDateTime localStart = LocalDateTime.of(current.toLocalDate(), hour.getStartTimeLocal());
                LocalDateTime localEnd = LocalDateTime.of(current.toLocalDate(), hour.getEndTimeLocal());
                ZonedDateTime zStart = localStart.atZone(zone).withZoneSameInstant(ZoneId.of("UTC"));
                ZonedDateTime zEnd = localEnd.atZone(zone).withZoneSameInstant(ZoneId.of("UTC"));
                windows.add(new Interval(zStart.toLocalDateTime(), zEnd.toLocalDateTime()));
            }
            current = current.plusDays(1);
        }
        return windows;
    }

    private long calculateOverlapMinutes(List<Interval> windows, LocalDateTime segStart, LocalDateTime segEnd, String status) {
        long minutes = 0;
        for (Interval window : windows) {
            LocalDateTime overlapStart = max(segStart, window.start);
            LocalDateTime overlapEnd = min(segEnd, window.end);
            if (overlapStart.isBefore(overlapEnd)) {
                minutes += ChronoUnit.MINUTES.between(overlapStart, overlapEnd);
            }
        }
        return minutes;
    }

    private LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    private static class Interval {
        LocalDateTime start, end;

        Interval(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }

    public Report getReport(String reportId) {
        return reportRepo.findById(reportId).orElseThrow();
    }
}
