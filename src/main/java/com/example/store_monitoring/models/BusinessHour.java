package com.example.store_monitoring.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalTime;

@Entity
@Data

@NoArgsConstructor
@AllArgsConstructor
public class BusinessHour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String storeId;
    private int dayOfWeek;
    private LocalTime startTimeLocal;
    private LocalTime endTimeLocal;

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getEndTimeLocal() {
        return endTimeLocal;
    }

    public void setEndTimeLocal(LocalTime endTimeLocal) {
        this.endTimeLocal = endTimeLocal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalTime getStartTimeLocal() {
        return startTimeLocal;
    }

    public void setStartTimeLocal(LocalTime startTimeLocal) {
        this.startTimeLocal = startTimeLocal;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
}
