package com.example.store_monitoring.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data

@NoArgsConstructor
@AllArgsConstructor
@IdClass(StoreStatusId.class)
public class StoreStatus {
    @Id
    private String storeId;
    @Id
    private LocalDateTime timestampUtc;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public LocalDateTime getTimestampUtc() {
        return timestampUtc;
    }

    public void setTimestampUtc(LocalDateTime timestampUtc) {
        this.timestampUtc = timestampUtc;
    }
}

