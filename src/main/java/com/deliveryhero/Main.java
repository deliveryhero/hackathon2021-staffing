package com.deliveryhero;

import com.deliveryhero.service.StaffingService;

public class Main {

    public static void main(final String[] args) {
        final StaffingService service = new StaffingService();
        service.doAssignments();
    }
}
