package com.deliveryhero.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
public class SlotAssignment {
    private int index;
    private Demand demand;
    private final List<Employee> assignedEmployees = new ArrayList<>();
    private double globalPenaltyImprovement;

    public SlotAssignment(final int index, final Demand demand) {
        this.index = index;
        this.demand = demand;
    }

    /**
     *
     * @param employee
     * @return local penalty improvement change since adding an employee.
     */
    public double addEmployee(final Employee employee) {
        assignedEmployees.add(employee);
        if (assignedEmployees.size() - demand.getDemand() == 0) {
            return - demand.getUnderStaffingPenalty() - demand.getOverStaffingPenalty();
        }
        return 0;
    }
    
    public List<Employee> getAssignedEmployees() {
        return Collections.unmodifiableList(assignedEmployees);
    }

    public double computeLocalPenaltyImprovement() {
        if (assignedEmployees.size() - demand.getDemand() < 0) {
            return demand.getUnderStaffingPenalty();
        } else {
            return -demand.getOverStaffingPenalty();
        }
    }

    public double computeLocalPenalty() {
        int supplyDemandDiff = assignedEmployees.size() - demand.getDemand();
        if (supplyDemandDiff < 0) {
            // under-staffing
            return demand.getUnderStaffingPenalty() * -supplyDemandDiff;
        } else {
            // sufficient staff
            return demand.getOverStaffingPenalty() * supplyDemandDiff;
        }
    }

    public double getGlobalPenaltyImprovement() {
        return globalPenaltyImprovement;
    }
}
