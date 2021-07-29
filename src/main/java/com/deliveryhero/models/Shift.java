package com.deliveryhero.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Shift {
    private final List<LocalDateTime> timeSlots = new ArrayList<>();
    
    public void addTimeSlot(final LocalDateTime slot) {
        timeSlots.add(slot);
    }

    public List<LocalDateTime> getTimeSlots() {
        return Collections.unmodifiableList(timeSlots);
    }       
}
