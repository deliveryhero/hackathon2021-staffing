package com.deliveryhero.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
public class SlotAssignment {
    private Demand demand;
    private final List<Employee> assignedEmployees = new ArrayList<>();
    private float globalPenaltyImprovement;

    public SlotAssignment(final Demand demand) {
        this.demand = demand;
    }
    
    public void addEmployee(final Employee employee) {
        assignedEmployees.add(employee);
    }
    
    public List<Employee> getAssignedEmployees() {
        return Collections.unmodifiableList(assignedEmployees);
    }

    public float getLocalPenaltyImprovement() {
        final int diff = assignedEmployees.size() - demand.getDemand();
        if (diff < 0) {
            // under-staffing
            return demand.getUnderStaffingPenalty() * -diff;
        } else {
            // sufficient staff
            return demand.getOverStaffingPenalty() * diff;
        }
    }
}
