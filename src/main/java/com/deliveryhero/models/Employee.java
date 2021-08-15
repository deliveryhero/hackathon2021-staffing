package com.deliveryhero.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.deliveryhero.service.DataService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private List<TimeRange> shiftsAndBreaks;
    private List<Shift> assignedShifts;
    private int weeklySlots;
    private int[] dailySlots;
    private int[] dailyShifts;
    private int minShiftDurationSlots;
    private int maxShiftDurationSlots;
    private int minDurationPerDaySlots;
    private int maxDurationPerDaySlots;
    private int minDurationPerWeekSlots;
    private int maxDurationPerWeekSlots;
    private int minBreakDurationSlots;

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
        this.minShiftDurationHours = minShiftDurationHours;
        this.minShiftsPerDay = minShiftsPerDay;
        this.maxShiftDurationHours = maxShiftDurationHours;
        this.minDurationPerDayHours = minDurationPerDayHours;
        this.maxDurationPerDayHours = maxDurationPerDayHours;
        this.minDurationPerWeekHours = minDurationPerWeekHours;
        this.maxDurationPerWeekHours = maxDurationPerWeekHours;
        this.minBreakDurationHours = minBreakDurationHours;
        this.unavailableTimes = new ArrayList<>();
        this.assignedShifts = new ArrayList<>();
        this.shiftsAndBreaks = new ArrayList<>();
    }

    public void addShift(final TimeRange shiftTime, Shift shift) {
        assignedShifts.add(shift);
        updateState(shift);
        unavailableTimes.add(new TimeRange(Instant.ofEpochSecond(shiftTime.getStart().getEpochSecond() - getMinBreakDurationHours() * 3600),
                Instant.ofEpochSecond(shiftTime.getEnd().getEpochSecond() + getMinBreakDurationHours() * 3600)));
    }

    private void updateState(Shift shift) {
        int shiftLength = shift.getLength();
        int day = shift.getDay();
        dailyShifts[day]++;
        dailySlots[day] += shiftLength;
        weeklySlots += shiftLength;
    }

    public List<TimeRange> getUnavailableTimes() {
        return unavailableTimes;
    }

    public boolean checkUnavailabilities(TimeRange newShift) {
        for (TimeRange timeBlock : unavailableTimes) {
            if (newShift.getStart().getEpochSecond() <= timeBlock.getEnd().getEpochSecond()
                    && newShift.getEnd().getEpochSecond() >= timeBlock.getStart().getEpochSecond()) {
                return false;
            }
        }
        return true;
    }

    public boolean canAddShift(Shift shift) {
        int shiftLength = shift.getLength();
        int day = shift.getDay();
        return dailyShifts[day] < maxShiftsPerDay
                && shiftLength >= minShiftDurationSlots
                && shiftLength <= maxShiftDurationSlots
                && dailySlots[day] + shiftLength <= maxDurationPerDaySlots
                && weeklySlots + shiftLength <= maxDurationPerWeekSlots;
    }

    public void initState(int numberDays) {
        dailySlots = new int[numberDays];
        dailyShifts = new int[numberDays];
        minShiftDurationSlots = minShiftDurationHours * DataService.numberSlotsPerHour;
        maxShiftDurationSlots = maxShiftDurationHours * DataService.numberSlotsPerHour;
        minDurationPerDaySlots = minDurationPerDayHours * DataService.numberSlotsPerHour;
        maxDurationPerDaySlots = maxDurationPerDayHours * DataService.numberSlotsPerHour;
        minDurationPerWeekSlots = minDurationPerWeekHours * DataService.numberSlotsPerHour;
        maxDurationPerWeekSlots = maxDurationPerWeekHours * DataService.numberSlotsPerHour;
        minBreakDurationSlots = minBreakDurationHours * DataService.numberSlotsPerHour;
    }

    public String toString() {
        return new StringBuilder().append(employeeId).append(" ").append(startingPointId).append(" ").append(
                assignedShifts.size()).append(" shifts with ").append(weeklySlots).append(" slots.\nDaily shifts: ")
                .append(Arrays.toString(dailyShifts)).append("\nDaily slots: ").append(Arrays.toString(dailySlots))
                .toString();

    }

    public void removeAssignedShift(Shift shift) {

    }
}
