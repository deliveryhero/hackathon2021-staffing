package com.deliveryhero.service;

import com.deliveryhero.models.Demand;
import com.deliveryhero.models.Employee;
import com.deliveryhero.models.RemainingSlots;
import com.deliveryhero.models.SlotAssignment;
import com.deliveryhero.models.TimeRange;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StaffingService {
    private final List<Demand> demandData = new ArrayList<>();
    private final List<Employee> employees = new ArrayList<>();
    private final Map<LocalDate, List<SlotAssignment>> weeklySlotMatrix = new HashMap<>();
    private List<SlotAssignment> currentSlotMatrix;
    private final Map<String, RemainingSlots> remainingSlots = new HashMap<>();

    public StaffingService() {
        final DataService dataService = new DataService();
        try {
            demandData.addAll(dataService.getDemandData());
            employees.addAll(dataService.getEmployeeData());
            initRemainingSlots();
            createSlotMatrix();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void doAssignments() {
        for (final Map.Entry<LocalDate, List<SlotAssignment>> entry : weeklySlotMatrix.entrySet()) {
            currentSlotMatrix = entry.getValue();
            resetDailyRemainingSlots();
            for (final Employee employee : employees) {
                allocateSlotsToEmployee(employee);
            }
        }
        printSolution();
    }

    private void resetDailyRemainingSlots() {
        for (final Employee employee : employees) {
            remainingSlots.get(employee.getEmployeeId())
                    .setSlotsRemainingInDay(employee.getMaxDurationPerDayHours() * 4);
        }
    }

    private void resetShiftRemainingSlots(Employee employee) {
        remainingSlots.get(employee.getEmployeeId())
                .setSlotsRemainingInShift(employee.getMaxShiftDurationHours() * 4);
    }

    private void initRemainingSlots() {
        for (final Employee employee : employees) {
            final int slotsRemainingInShift = employee.getMaxShiftDurationHours() * 4;
            final int slotsRemainingInDay = employee.getMaxDurationPerDayHours() * 4;
            final int slotsRemainingInWeek = employee.getMaxDurationPerWeekHours() * 4;
            remainingSlots.put(employee.getEmployeeId(),
                    new RemainingSlots(slotsRemainingInShift, slotsRemainingInDay, slotsRemainingInWeek));
        }
    }

    private void printSolution() {
        for (final Map.Entry<LocalDate, List<SlotAssignment>> entry : weeklySlotMatrix.entrySet()) {
            for (final SlotAssignment row : entry.getValue()) {
                final List<String> assignedEmployees =
                        row.getAssignedEmployees().stream().map(emp -> emp.getEmployeeId())
                                .collect(Collectors.toList());
                final String output = String.join("\t", row.getDemand().getStartingPointId(), row.getDemand().getTimestamp().toString(),
                        String.valueOf(row.getDemand().getDemand()), String.valueOf(row.getAssignedEmployees().size()),
                        String.valueOf(row.computeLocalPenalty()), String.join(",", assignedEmployees));
                System.out.println(output);
            }
        }
    }

    private void createSlotMatrix() {
        for (final Map.Entry<LocalDate, List<Demand>> entry : getDailyDemand().entrySet()) {
            final List<SlotAssignment> matrix = new ArrayList<>();
            for (final Demand demand : entry.getValue()) {
                matrix.add(new SlotAssignment(demand));
            }
            weeklySlotMatrix.put(entry.getKey(), matrix);
        }
    }

    private Map<LocalDate, List<Demand>> getDailyDemand() {
        return demandData.stream().collect(Collectors.groupingBy(Demand::getDate));
    }

    private void allocateSlotsToEmployee(final Employee employee) {
        final List<TimeRange> unavailableTimes = employee.getUnavailableTimes();
        for (int shiftIndex = 0; shiftIndex < employee.getMaxShiftsPerDay(); shiftIndex++) {
            resetShiftRemainingSlots(employee);
            final int slotsToAssign = getRemainingSlotsCount(employee);
            if (slotsToAssign < employee.getMinShiftDurationHours() * 4) {
                break;
            }
            computeGlobalImprovements(employee, slotsToAssign);
            allocateForShift(employee, slotsToAssign);
        }
    }

    private void allocateForShift(final Employee employee, final int slotsToAssign) {
        final int bestSlot = getBestSlot(employee, slotsToAssign);
        if (bestSlot == -1) {
            return;
        }
        final int endSlot = getEndSlot(bestSlot, slotsToAssign);
        for (int i = bestSlot; i <= endSlot; i++) {
            assign(employee, i);
        }
        employee.addShift(new TimeRange(currentSlotMatrix.get(bestSlot).getDemand().getUnixTime(),
                currentSlotMatrix.get(endSlot).getDemand().getUnixTime()));
    }

    private int getEndSlot(int startSlot, int slotsToAssign) {
        return Math.min(startSlot + slotsToAssign, currentSlotMatrix.size()) - 1;
    }

    private int getRemainingSlotsCount(final Employee employee) {
        final RemainingSlots slots = remainingSlots.get(employee.getEmployeeId());
        return Math.min(slots.getSlotsRemainingInShift(),
                Math.min(slots.getSlotsRemainingInDay(), slots.getSlotsRemainingInWeek()));
    }

    private void assign(final Employee employee, final int slotIndex) {
        if (slotIndex >= currentSlotMatrix.size()) {
            return;
        }
        final SlotAssignment slot = currentSlotMatrix.get(slotIndex);
        slot.addEmployee(employee);
        remainingSlots.get(employee.getEmployeeId()).decrement();
    }

    private int getBestSlot(final Employee employee, int slotsToAssign) {
        int result = -1;
        float max = Float.MIN_VALUE;
        for (int i = 0; i < currentSlotMatrix.size(); i++) {
            final boolean improvesPenalty = currentSlotMatrix.get(i).getGlobalPenaltyImprovement() > max;
            /// TODO should consider min break if it is assigned to the same rider.
            // To check that the shift intercepts any of the time range in Employee.ShiftsAndBreak
            //final boolean alreadyAssigned = currentSlotMatrix.get(i).getAssignedEmployees().stream()
            //        .anyMatch(emp -> emp.getEmployeeId().equals(employee.getEmployeeId()));
            final boolean isShiftFeasible = employee.checkShiftsAndBreaks(new TimeRange(
                    currentSlotMatrix.get(i).getDemand().getUnixTime(),
                    currentSlotMatrix.get(getEndSlot(i, slotsToAssign)).getDemand().getUnixTime()));
            if (improvesPenalty && isShiftFeasible) {
                result = i;
                max = currentSlotMatrix.get(i).getGlobalPenaltyImprovement();
            }
        }
        return result;
    }

    private List<SlotAssignment> computeGlobalImprovements(final Employee employee, final int shiftSize) {
        for (int i = 0; i < currentSlotMatrix.size(); i++) {
            final float globalImprovement = getGlobalImprovement(i, shiftSize);
            currentSlotMatrix.get(i).setGlobalPenaltyImprovement(globalImprovement);
        }
        return currentSlotMatrix;
    }

    private float getGlobalImprovement(final int rowIndex, final int lookAheadSlots) {
        float result = 0;
        for (int i = rowIndex; i <= rowIndex + lookAheadSlots; i++) {
            result += currentSlotMatrix.get(rowIndex).computeLocalPenaltyImprovement();
        }
        return result;
    }
}
