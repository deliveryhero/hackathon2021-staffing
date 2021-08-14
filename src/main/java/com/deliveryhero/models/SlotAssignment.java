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
    private float globalPenaltyImprovement;

    public SlotAssignment(final int index, final Demand demand) {
        this.index = index;
        this.demand = demand;
    }
    
    public void addEmployee(final Employee employee) {
        assignedEmployees.add(employee);
    }
    
    public List<Employee> getAssignedEmployees() {
        return Collections.unmodifiableList(assignedEmployees);
    }

    public float computeLocalPenaltyImprovement() {
        if (assignedEmployees.size() - demand.getDemand() < 0) {
            return demand.getUnderStaffingPenalty();
        } else {
            return -demand.getOverStaffingPenalty();
        }
    }

    public float computeLocalPenalty() {
        int supplyDemandDiff = assignedEmployees.size() - demand.getDemand();
        if (supplyDemandDiff < 0) {
            // under-staffing
            return demand.getUnderStaffingPenalty() * -supplyDemandDiff;
        } else {
            // sufficient staff
            return demand.getOverStaffingPenalty() * supplyDemandDiff;
        }
    }

    public float getGlobalPenaltyImprovement() {
        return globalPenaltyImprovement;
    }
}
