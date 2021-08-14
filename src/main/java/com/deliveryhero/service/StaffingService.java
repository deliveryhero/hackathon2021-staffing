package com.deliveryhero.service;

import com.deliveryhero.models.Demand;
import com.deliveryhero.models.Employee;
import com.deliveryhero.models.RemainingSlots;
import com.deliveryhero.models.SlotAssignment;
import com.deliveryhero.models.TimeRange;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class StaffingService {
    private final List<Demand> demandData = new ArrayList<>();
    private final List<Employee> employees = new ArrayList<>();
    private final Map<LocalDate, List<SlotAssignment>> weeklySlotMatrix = new TreeMap<>();
    private final SlotAssignment[] allSlots = new SlotAssignment[demandData.size()];
    private List<SlotAssignment> currentSlotMatrix;
    private final Map<String, RemainingSlots> remainingSlots = new HashMap<>();
    private long startTime;

    public StaffingService() {
        final DataService dataService = new DataService();
        try {
            demandData.addAll(dataService.getDemandData());
            employees.addAll(dataService.getEmployeeData());
            startTime = System.nanoTime();
            initDays();
            initRemainingSlots();
            //createSlotMatrix();
            createAllSlots();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initDays() {
        System.out.println(demandData.stream().collect(Collectors.groupingBy(Demand::getDate)));
    }

    private void createAllSlots() {
        for (int i = 0; i < demandData.size(); i++) {
            allSlots[i] = new SlotAssignment(i, demandData.get(i));
        }
    }

    // This is for randomizing the days
//    public void doAssignments() {
//        for (final Employee employee : employees) {
//            final List<List<SlotAssignment>> dailyMatrices = new ArrayList<>(weeklySlotMatrix.values());
//            Collections.shuffle(dailyMatrices);
//            for (final List<SlotAssignment> dailyMatrix : dailyMatrices) {
//                currentSlotMatrix = dailyMatrix;
//                resetDailyRemainingSlots();
//                allocateSlotsToEmployee(employee);
//            }
//        }
//        printSolution();
//    }

    // TODO currentSlotMatrix is day-based. should make it week-based.
    public void doAssignments() {
        for (final Map.Entry<LocalDate, List<SlotAssignment>> entry : weeklySlotMatrix.entrySet()) {
            currentSlotMatrix = entry.getValue();
            resetDailyRemainingSlots();
            for (int shiftIndex = 0; shiftIndex < employees.get(0).getMaxShiftsPerDay(); shiftIndex++) {
                //Collections.shuffle(employees);
                for (final Employee employee : employees) {
                    resetShiftRemainingSlots(employee);
                    final int slotsToAssign = getRemainingSlotsCount(employee);
                    if (slotsToAssign < employee.getMinShiftDurationHours() * DataService.numberSlotsPerHour) {
                        break;
                    }
                    allocateSlotsToEmployee(employee, slotsToAssign);
                }
            }
        }

        long elapsedTime = (System.nanoTime() - startTime) / 1000000;
        System.out.println(elapsedTime);

        localSearch();

        printSolution();
    }

    private void localSearch() {
    }

//    private void resetDailyRemainingSlots() {
//        for (final Employee employee : employees) {
//            remainingSlots.get(employee.getEmployeeId())
//                    .setSlotsRemainingInDay(employee.getMaxDurationPerDayHours() * DataService.numberSlotsPerHour);
//        }
//    }

    private void resetShiftRemainingSlots(final Employee employee) {
        remainingSlots.get(employee.getEmployeeId())
                //.setSlotsRemainingInShift(employee.getMaxShiftDurationHours() * DataService.numberSlotsPerHour);
                // TODO hardcoded here for 3.5 hours
                .setSlotsRemainingInShift(14);
    }

    // TODO compute number of slots per hour (e.g. 4 here) by slot size (e.g. 15 minutes).
    private void initRemainingSlots() {
        for (final Employee employee : employees) {
            //final int slotsRemainingInShift = employee.getMaxShiftDurationHours() * DataService.numberSlotsPerHour;
            final int slotsRemainingInShift = 14;
                    //(employee.getMinShiftDurationHours() + employee.getMaxShiftDurationHours())/ 2 * DataService.numberSlotsPerHour;

            System.out.println(slotsRemainingInShift);
            System.exit(0);
            final int slotsRemainingInDay = employee.getMaxDurationPerDayHours() * DataService.numberSlotsPerHour;
            final int slotsRemainingInWeek = employee.getMaxDurationPerWeekHours() * DataService.numberSlotsPerHour;
            remainingSlots.put(employee.getEmployeeId(),
                    new RemainingSlots(slotsRemainingInShift, slotsRemainingInDay, slotsRemainingInWeek));
        }
    }

    private void printSolution() {
        double cost = 0;
        for (final Map.Entry<LocalDate, List<SlotAssignment>> entry : weeklySlotMatrix.entrySet()) {
            for (final SlotAssignment row : entry.getValue()) {
                final List<String> assignedEmployees =
                        row.getAssignedEmployees().stream().map(emp -> emp.getEmployeeId())
                                .collect(Collectors.toList());
                final String output = String.join("\t", row.getDemand().getStartingPointId(),
                        row.getDemand().getTimestamp().toString(),
                        String.valueOf(row.getDemand().getDemand()), String.valueOf(row.getAssignedEmployees().size()),
                        String.format("%.2f", row.computeLocalPenalty()), String.join(",", assignedEmployees));
                System.out.println(output);
                cost += row.computeLocalPenalty();
            }
        }
        System.out.println(cost);
        for (final Employee e : employees) {
            System.out.println(e.getShifts().size());
            for (final int[] shift : e.getShifts()) {
                System.out.println(Arrays.toString(shift));
            }
        }
    }

    private void createSlotMatrix() {
        int index = 0;
        for (final Map.Entry<LocalDate, List<Demand>> entry : getDailyDemand().entrySet()) {
            final List<SlotAssignment> matrix = new ArrayList<>();
            for (final Demand demand : entry.getValue()) {
                matrix.add(new SlotAssignment(index++, demand));
            }
            weeklySlotMatrix.put(entry.getKey(), matrix);
        }
    }

    private Map<LocalDate, List<Demand>> getDailyDemand() {
        return demandData.stream().collect(Collectors.groupingBy(Demand::getDate));
    }

    private void allocateSlotsToEmployee(final Employee employee, int slotsToAssign) {
        computeGlobalImprovements(employee, slotsToAssign);
        allocateForShift(employee, slotsToAssign);
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
                currentSlotMatrix.get(endSlot).getDemand().getUnixTime()), new int[]{bestSlot, endSlot});
    }

    private int getEndSlot(final int startSlot, final int slotsToAssign) {
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

    private int getBestSlot(final Employee employee, final int slotsToAssign) {
        int result = -1;
        float max = 0;
        for (int i = 0; i < currentSlotMatrix.size(); i++) {
            final boolean improvesPenalty = currentSlotMatrix.get(i).getGlobalPenaltyImprovement() > max;           
            final boolean isShiftFeasible = employee.checkUnavailabilities(new TimeRange(
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
        for (int i = rowIndex; i <= rowIndex + lookAheadSlots && i < currentSlotMatrix.size(); i++) {
            result += currentSlotMatrix.get(i).computeLocalPenaltyImprovement();
        }
        return result;
    }
}
