package com.deliveryhero.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
    private final List<TimeRange> unavailableTimes = new ArrayList<>();
    private final List<TimeRange> shifts = new ArrayList<>();
    private final List<TimeRange> shiftsAndBreaks = new ArrayList<>();

    public void addShift(final TimeRange shift) {
        shifts.add(shift);
        shiftsAndBreaks.add(new TimeRange(Instant.ofEpochSecond(shift.getStart().getEpochSecond() - getMinBreakDurationHours() * 3600),
                Instant.ofEpochSecond(shift.getEnd().getEpochSecond() + getMinBreakDurationHours() * 3600)));
    }

    public List<TimeRange> getUnavailableTimes() {
        return unavailableTimes;
    }
}
