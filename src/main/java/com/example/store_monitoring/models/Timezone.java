package com.example.store_monitoring.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Data

@NoArgsConstructor
@AllArgsConstructor
public class Timezone {
    @Id
    private String storeId;
    private String timezoneStr;

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getTimezoneStr() {
        return timezoneStr;
    }

    public void setTimezoneStr(String timezoneStr) {
        this.timezoneStr = timezoneStr;
    }
}
