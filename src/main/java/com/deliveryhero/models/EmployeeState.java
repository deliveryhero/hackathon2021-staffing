package com.deliveryhero.models;

import com.deliveryhero.service.DataService;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;

@Data
public class EmployeeState {
    private int slotsRemainingInShift, slotsRemainingInWeek;
    private int[] slotsRemainingInDay;

    public EmployeeState(int slotsRemainingInShift, int slotsRemainingInDay, int slotsRemainingInWeek) {
        this.slotsRemainingInShift = slotsRemainingInShift;
        this.slotsRemainingInDay = new int[DataService.days.length];
        Arrays.fill(this.slotsRemainingInDay, slotsRemainingInDay);
    }

    public void decrement(int day) {
        if(slotsRemainingInShift > 0)
            slotsRemainingInShift--;
        if(slotsRemainingInDay[day] > 0)
            slotsRemainingInDay[day]--;
        if(slotsRemainingInWeek > 0)
            slotsRemainingInWeek--;
    }
}
