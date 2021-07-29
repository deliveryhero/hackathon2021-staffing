package com.deliveryhero.service;

import com.deliveryhero.models.Demand;
import com.deliveryhero.models.Employee;
import com.deliveryhero.models.Shift;
import com.deliveryhero.models.SlotAssignment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffingService {
    private final List<Demand> demandData = new ArrayList<>();
    private final List<Employee> employees = new ArrayList<>();
    final List<SlotAssignment> slotMatrix;
    final Map<String, List<Shift>> employeeSlotsMap;

    public StaffingService() {
        final DataService dataService = new DataService();
        try {
            demandData.addAll(dataService.getDemandData());
            employees.addAll(dataService.getEmployeeData());
            slotMatrix = createSlotMatrix();
            employeeSlotsMap = createEmployeeSlotsMap();
            System.out.println(demandData);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void doAssignments() {
        for(final Employee employee : employees) {
            allocateSlotsToEmployee(employee);
        }
    }

    private Map<String, List<Shift>> createEmployeeSlotsMap() {
        final Map<String, List<Shift>> result = new HashMap<>();
        for (final Employee employee : employees) {
            result.put(employee.getEmployeeId(), new ArrayList<>());
        }
        return result;
    }

    private List<SlotAssignment> createSlotMatrix() {
        final List<SlotAssignment> matrix = new ArrayList<>();
        for (final Demand demand : demandData) {
            matrix.add(new SlotAssignment(demand));
        }
        return matrix;
    }

    private void allocateSlotsToEmployee(final Employee employee) {
        final List<SlotAssignment> slotMatrix = computeGlobalImprovements(employee);
        for (int i = 0; i < employee.getMaxShiftsPerDay(); i++) {
            allocateForShift(employee, i);
        }
    }

    private void allocateForShift(final Employee employee, final int shiftIndex) {
        final int bestSlot = getBestSlot(employee);
        if (bestSlot == -1) {
            return;
        }
        assign(employee, bestSlot, shiftIndex);
    }

    private void assignAround(final Employee employee, final int bestSlot, final int numSlots) {
        for (int i = 1; i <= numSlots; i++) {
            
        }
    }

    private int getRemainingSlotsCount(final Employee employee, final int shiftIndex) {
        final int numSlotsAlreadyAssignedInShift =
                employeeSlotsMap.get(employee.getEmployeeId()).get(shiftIndex).getTimeSlots().size();
        final int numSlotsAlreadyAssignedInDay = employeeSlotsMap.get(employee.getEmployeeId()).stream()
                .mapToInt(shift -> shift.getTimeSlots().size()).sum();
        final int remainingInShift = employee.getMaxShiftDurationHours() * 4 - numSlotsAlreadyAssignedInShift;
        final int remainingInDay = employee.getMaxDurationPerDayHours() * 4 - numSlotsAlreadyAssignedInDay;
        return Math.min(remainingInShift, remainingInDay);
    }

    private void assign(final Employee employee, final int slotIndex, final int shiftIndex) {
        final SlotAssignment slot = slotMatrix.get(slotIndex);
        slot.addEmployee(employee);
        final List<Shift> shifts = employeeSlotsMap.get(employee.getEmployeeId());
        if (shifts.size() >= shiftIndex) {
            shifts.add(new Shift());
        }
        shifts.get(shiftIndex).addTimeSlot(slot.getDemand().getTimestamp());
    }

    private int getBestSlot(final Employee employee) {
        int result = -1;
        float max = 0;
        for (int i = 0; i < slotMatrix.size(); i++) {
            final boolean improvesPenalty = slotMatrix.get(i).getGlobalPenaltyImprovement() > max;
            final boolean alreadyAssigned = slotMatrix.get(i).getAssignedEmployees().stream()
                    .anyMatch(emp -> emp.getEmployeeId().equals(employee.getEmployeeId()));
            if (improvesPenalty && !alreadyAssigned) {
                result = i;
                max = slotMatrix.get(i).getGlobalPenaltyImprovement();
            }
        }
        return result;
    }

    private List<SlotAssignment> computeGlobalImprovements(final Employee employee) {
        final int numSlots = employee.getMaxShiftDurationHours() * 4;
        // add the logic numSlots = min(max shift duration, remaining hours per day,
        // remaining hours per week)

        for (int i = 0; i < slotMatrix.size(); i++) {
            final float globalImprovement = getGlobalImprovement(i, numSlots);
            slotMatrix.get(i).setGlobalPenaltyImprovement(globalImprovement);
        }
        return slotMatrix;
    }

    private float getGlobalImprovement(final int rowIndex, final int lookAheadSlots) {
        float result = 0;
        for (int i = rowIndex; i <= rowIndex + lookAheadSlots; i++) {
            result += slotMatrix.get(rowIndex).getLocalPenaltyImprovement();
        }
        return result;
    }
}
