package com.deliveryhero.service;

import com.deliveryhero.models.Demand;
import com.deliveryhero.models.Employee;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StaffingService {
    private final List<Demand> demand = new ArrayList<>();
    private final List<Employee> employees = new ArrayList<>();
    
    public StaffingService() {
        final DataService dataService = new DataService();
        try {
            demand.addAll(dataService.getDemandData());
            employees.addAll(dataService.getEmployeeData());
            System.out.println(demand);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
