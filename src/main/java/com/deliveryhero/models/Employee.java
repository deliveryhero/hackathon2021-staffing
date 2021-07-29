package com.deliveryhero.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Employee {
    private String employeeId;
    private String startingPointId;
    private int maxShiftsPerDay;
    private int minShiftDurationHours;
    private int minShiftsPerDay;
    private int maxShiftDurationHours;
    private int minDurationPerDayHours;
    private int maxDurationPerDayHours;
    private int minDurationPerWeekHours;
    private int maxDurationPerWeekHours;
    private int minBreakDurationHours;    
}
