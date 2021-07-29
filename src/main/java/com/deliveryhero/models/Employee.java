package com.deliveryhero.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    private List<TimeRange> unavailableTimes;
    private List<TimeRange> shifts;
    private List<TimeRange> shiftsAndBreaks;

    public Employee(String employeeId,
            String startingPointId,
            int maxShiftsPerDay,
            int minShiftDurationHours,
            int minShiftsPerDay,
            int maxShiftDurationHours,
            int minDurationPerDayHours,
            int maxDurationPerDayHours,
            int minDurationPerWeekHours,
            int maxDurationPerWeekHours,
            int minBreakDurationHours) {

        this.employeeId = employeeId;
        this.startingPointId = startingPointId;
        this.maxShiftsPerDay = maxShiftsPerDay;
        this.minBreakDurationHours = minShiftDurationHours;
        this.minShiftsPerDay = minShiftsPerDay;
        this.maxShiftDurationHours = maxShiftDurationHours;
        this.minDurationPerDayHours = minDurationPerDayHours;
        this.maxDurationPerDayHours = maxDurationPerDayHours;
        this.minDurationPerWeekHours = minDurationPerWeekHours;
        this.maxDurationPerWeekHours = maxDurationPerWeekHours;
        this.minBreakDurationHours = minBreakDurationHours;
        this.unavailableTimes = new ArrayList<>();
        this.shifts = new ArrayList<>();
        this.shiftsAndBreaks = new ArrayList<>();
    }

    public void addShift(TimeRange shift) {
        shifts.add(shift);
        shiftsAndBreaks.add(new TimeRange(Instant.ofEpochSecond(shift.getStart().getEpochSecond() - getMinBreakDurationHours() * 3600),
                Instant.ofEpochSecond(shift.getEnd().getEpochSecond() + getMinBreakDurationHours() * 3600)));
    }

    public List<TimeRange> getUnavailableTimes() {
        return unavailableTimes;
    }
}
