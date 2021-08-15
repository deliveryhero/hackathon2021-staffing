package com.deliveryhero.service;

import com.deliveryhero.models.*;
import com.deliveryhero.util.Randomizer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.SECONDS;

public class StaffingService {
    private final List<Demand> demandData = new ArrayList<>();
    private final List<Employee> employees = new ArrayList<>();
    private final Map<LocalDate, List<SlotAssignment>> weeklySlotMatrix = new TreeMap<>();
    private SlotAssignment[] allSlots;
    private final Map<String, RemainingSlots> remainingSlots = new HashMap<>();
    private long startTime;
    private int defaultSlotsPerShift = 14;
    private Shift[] shiftsFixedSize;
    private int numberSlots;
    private List<Shift> tabuShifts = new ArrayList<>();

    public StaffingService() {
        final DataService dataService = new DataService();
        try {
            demandData.addAll(dataService.getDemandData());
            numberSlots = demandData.size();
            employees.addAll(dataService.getEmployeeData());
            //initRemainingSlots();
            //createSlotMatrix();
            createAllSlots();
            initUnavailabilities();
            startTime = System.nanoTime();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initUnavailabilities() {
        Instant weekStart = demandData.get(0).getUnixTime();
        Instant weekEnd = Instant.ofEpochSecond(demandData.get(numberSlots - 1).getUnixTime().getEpochSecond()
                + DataService.slotInterval);
        for (Employee employee : employees) {
            Iterator<TimeRange> unavailableTimes = employee.getUnavailableTimes().iterator();
            TimeRange times;
            while (unavailableTimes.hasNext()) {
                times = unavailableTimes.next();
                if (times.getEnd().compareTo(weekStart) > 0 && times.getStart().compareTo(weekEnd) < 0) {
                    int slotStart = Math.max(0, (int) SECONDS.between(weekStart, times.getStart()) / DataService.slotInterval);
                    int slotEnd = (int) Math.min(numberSlots - 1,
                            Math.ceil(SECONDS.between(weekStart, times.getEnd()) * 1.0 / DataService.slotInterval) - 1);
                    employee.addUnavailableSlot(new SlotRange(slotStart, slotEnd));

                }
            }
        }
    }

    private void createAllSlots() {
        allSlots = new SlotAssignment[numberSlots];
        for (int i = 0; i < numberSlots; i++) {
            allSlots[i] = new SlotAssignment(i, demandData.get(i));
        }
    }

    // TODO currentSlotMatrix is day-based. should make it week-based.
    public void doAssignments() {
        System.out.println(computeTotalCost());
        initShiftEvaluations();
        initEmployees();
        Iterator<Employee> employeeIterator;
        List<Shift> bestShifts;
        while (! (bestShifts = pickBestShifts()).isEmpty()) {
            while (! bestShifts.isEmpty()) {
                Shift shift = Randomizer.nextElement(bestShifts);
                employeeIterator = Randomizer.shuffledIterator(employees);
                boolean assigned = false;
                while(employeeIterator.hasNext()) {
                    Employee employee = employeeIterator.next();
                    if (canAssign(shift, employee)) {
                        assignShift(shift, employee);
                        Map<Integer, Double> shiftEvalChanges = computeShiftEvaluationChanges(shift, employee);
                        updateBestShifts(bestShifts, shiftEvalChanges.keySet());
                        assigned = true;
                        break;
                    }
                }
                if (! assigned) {
                    System.out.println("shift " + shift + " cannot be assigned. Removed and tabu.");
                    tabuShifts.add(shift);
                    bestShifts.remove(shift);
                }
            }
        }

        long elapsedTime = (System.nanoTime() - startTime) / 1000000;
        System.out.println("Time: " + elapsedTime);

        //localSearch();

        printSolution();
    }

    private void updateBestShifts(List<Shift> bestShifts, Set<Integer> changedSlots) {
        for (int slot : changedSlots) {
            ListIterator<Shift> iterator = bestShifts.listIterator();
            while(iterator.hasNext()) {
                if (iterator.next().containSlot(slot)) {
                    iterator.remove();
                }
            }
        }
    }

    private List<Shift> pickBestShifts() {
        double max = 0;
        List<Shift> bestShifts = new ArrayList<>();
        for (Shift shift : shiftsFixedSize) {
            if (! tabuShifts.contains(shift)) {
                if (shift.getEvaluation() > max) {
                    max = shift.getEvaluation();
                    bestShifts.clear();
                }
                if (shift.getEvaluation() >= max) {
                    bestShifts.add(shift);
                }
            }
        }
        return bestShifts;
    }

    private void assignShift(Shift shift, Employee employee) {
        employee.addShift(shift);
    }

    private Map<Integer, Double> computeShiftEvaluationChanges(Shift shift, Employee employee) {
        double diff;
        Map<Integer, Double> shiftEvalChanges = new HashMap<>();
        for (int i = shift.getStart(); i <= shift.getEnd(); i++) {
            diff = allSlots[i].addEmployee(employee);
            if (diff != 0) {
                shiftEvalChanges.put(i, diff);
            }
        }
        if (!shiftEvalChanges.isEmpty()) {
            updateShifts(shiftEvalChanges);
        }
        return shiftEvalChanges;
    }

    private void updateShifts(Map<Integer, Double> shiftEvalChanges) {
        for (int index : shiftEvalChanges.keySet()) {
            for (Shift shift : shiftsContainingIndex(index)) {
                shift.updateEvaluation(shiftEvalChanges.get(index));
            }
        }
    }

    private List<Shift> shiftsContainingIndex(int index) {
        List<Shift> shifts = new ArrayList<>(defaultSlotsPerShift);
        for (int i = index - defaultSlotsPerShift + 1; i <= index; i++) {
            shifts.add(shiftsFixedSize[i]);
        }
        return shifts;
    }

    private boolean canAssign(Shift shift, Employee employee) {
        if (! employee.canAddShift(shift)) {
            return false;
        }
        if (! employee.checkUnavailabilities(shift)) {
//            System.out.println("cannot add shift " + shift + " to employee " + employee + " due to unavailability");
//            System.out.println(employee.getUnavailableSlots());
//            System.out.println(employee.getShiftAndBreakSlots());
            return false;
        }
        return true;
    }

    private void initEmployees() {
        for (Employee employee: employees) {
            employee.initState(DataService.days.length);
        }
    }

    private void initShiftEvaluations() {
        double[] shiftEvals = evaluateAllShiftsFixedSize(defaultSlotsPerShift);
        CreateShifts(shiftEvals);
    }

    private void CreateShifts(double[] shiftEvals) {
        int numberShifts = shiftEvals.length;
        shiftsFixedSize = new Shift[numberShifts];
        for (int i = 0; i < numberShifts; i++) {
            shiftsFixedSize[i] = new Shift(i, i + defaultSlotsPerShift - 1, shiftEvals[i]);
        }
    }

    private void localSearch() {
        switchEmployees();
        //reduceShifts();
    }

    private void switchEmployees() {
        Iterator<Employee> employeeIterator = Randomizer.shuffledIterator(employees);
        Employee employee;
        Shift shift;
        while (employeeIterator.hasNext()) {
            employee = employeeIterator.next();
            if (employee.getWeeklySlots() < employee.getMinDurationPerWeekSlots()) {
                Employee employeeToGive = findMaxShiftEmployee();
                Iterator<Shift> shiftsToGive = Randomizer.shuffledIterator(employeeToGive.getAssignedShifts());
                while (shiftsToGive.hasNext()) {
                    shift = shiftsToGive.next();
                    if (canAssign(shift, employee)) {
                        switchShiftToEmployee(shift, employee, employeeToGive);

                    }
                }
            }
        }
    }

    private void switchShiftToEmployee(Shift shift, Employee employee, Employee employeeToGive) {
        assignShift(shift, employee);
        employeeToGive.removeAssignedShift(shift);
    }

    private Employee findMaxShiftEmployee() {
        int max = 0;
        Employee maxEmployee;
        Iterator<Employee> employeeIterator = Randomizer.shuffledIterator(employees);
        Employee employee = null;
        while (employeeIterator.hasNext()) {
            employee = employeeIterator.next();
            if (employee.getWeeklySlots() > max) {
                max = employee.getWeeklySlots();
                maxEmployee = employee;
            }
        }
        return employee;
    }

    private void resetShiftRemainingSlots(final Employee employee) {
        remainingSlots.get(employee.getEmployeeId())
                //.setSlotsRemainingInShift(employee.getMaxShiftDurationHours() * DataService.numberSlotsPerHour);
                // TODO hardcoded here for 3.5 hours
                .setSlotsRemainingInShift(14);
    }

    // TODO compute number of slots per hour (e.g. 4 here) by slot size (e.g. 15 minutes).
//    private void initRemainingSlots() {
//        for (final Employee employee : employees) {
//            //final int slotsRemainingInShift = employee.getMaxShiftDurationHours() * DataService.numberSlotsPerHour;
//            final int slotsRemainingInShift = 14;
//                    //(employee.getMinShiftDurationHours() + employee.getMaxShiftDurationHours())/ 2 * DataService.numberSlotsPerHour;
//
//            final int slotsRemainingInDay = employee.getMaxDurationPerDayHours() * DataService.numberSlotsPerHour;
//            final int slotsRemainingInWeek = employee.getMaxDurationPerWeekHours() * DataService.numberSlotsPerHour;
//            remainingSlots.put(employee.getEmployeeId(),
//                    new RemainingSlots(slotsRemainingInShift, slotsRemainingInDay, slotsRemainingInWeek));
//        }
//    }

    private void printSolution() {
        double cost = computeTotalCost();
        for (final SlotAssignment row : allSlots) {
            final List<String> assignedEmployees =
                    row.getAssignedEmployees().stream().map(emp -> emp.getEmployeeId())
                            .collect(Collectors.toList());
            final String output = String.join("\t", row.getDemand().getStartingPointId(),
                    String.valueOf(row.getIndex()),
                    row.getDemand().getTimestamp().toString(),
                    String.valueOf(row.getDemand().getDemand()), String.valueOf(row.getAssignedEmployees().size()),
                    String.format("%.2f", row.computeLocalPenalty()), String.join(",", assignedEmployees));
            System.out.println(output);
        }
        System.out.println(cost);
        System.out.println(String.join(" ", "Demand", String.valueOf(computeTotalDemand()), "Supply",
                String.valueOf(computeTotalSupply())));
        for (final Employee e : employees) {
            System.out.println(e);
            for (final Shift shift : e.getAssignedShifts()) {
                System.out.println(shift);
            }
        }
        System.out.println(tabuShifts.size());
        System.out.println(tabuShifts);

    }

    private double computeTotalCost() {
        double cost = 0;
        for (final SlotAssignment slot : allSlots) {
            cost += slot.computeLocalPenalty();
        }
        return cost;
    }

    private double computeTotalDemand() {
        int demand = 0;
        for (final SlotAssignment slot : allSlots) {
            demand += slot.getDemand().getDemand();
        }
        return demand;
    }

    private double computeTotalSupply() {
        int supply = 0;
        for (final SlotAssignment slot : allSlots) {
            supply += slot.getAssignedEmployees().size();
        }
        return supply;
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

    private int getRemainingSlotsCount(final Employee employee) {
        final RemainingSlots slots = remainingSlots.get(employee.getEmployeeId());
        //return Math.min(slots.getSlotsRemainingInShift(), Math.min(slots.getSlotsRemainingInDay(), slots.getSlotsRemainingInWeek()));
        return 0;
    }

    private double[] evaluateAllShiftsFixedSize(final int shiftSize) {
        double[] shiftEvals = new double[allSlots.length - shiftSize + 1];
        Arrays.fill(shiftEvals, Double.MIN_VALUE);
        shiftEvals[0] = evaluateOneShift(0, shiftSize);
        for (int i = 1; i < shiftEvals.length; i++) {
            shiftEvals[i] = shiftEvals[i-1] - allSlots[i-1].computeLocalPenaltyImprovement()
                    + allSlots[i + shiftSize - 1].computeLocalPenaltyImprovement();
        }
        return shiftEvals;
    }

    private double evaluateOneShift(int startIndex, int shiftSize) {
        if (startIndex + shiftSize >= allSlots.length) {
            return Double.MIN_VALUE;
        }
        double result = 0;
        for (int i = startIndex; i < startIndex + shiftSize && i < allSlots.length; i++) {
            result += allSlots[i].computeLocalPenaltyImprovement();
        }
        return result;
    }
}
