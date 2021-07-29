package com.deliveryhero.models;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Demand {
    private String startingPointId;
    private Instant unixTime;
    private LocalDateTime timestamp;
    private int demand;
    private float underStaffingPenalty;
    private float overStaffingPenalty;
    
    public LocalDate getDate() {
        return timestamp.toLocalDate();
    }
}
