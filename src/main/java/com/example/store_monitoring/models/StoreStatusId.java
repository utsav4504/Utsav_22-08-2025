package com.example.store_monitoring.models;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data

@NoArgsConstructor
@AllArgsConstructor
public class StoreStatusId implements Serializable {
    private String storeId;
    private LocalDateTime timestampUtc;

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