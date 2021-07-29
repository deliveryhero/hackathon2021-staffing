package com.deliveryhero.service;

import com.deliveryhero.models.Demand;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StaffingService {
    private final List<Demand> demand = new ArrayList<>();
    
    public StaffingService() {
        final DataService dataService = new DataService();
        try {
            demand.addAll(dataService.getDemandData());
            System.out.println(demand);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
