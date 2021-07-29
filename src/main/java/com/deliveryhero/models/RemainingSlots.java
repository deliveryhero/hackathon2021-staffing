package com.deliveryhero.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RemainingSlots {
    private int slotsRemainingInShift, slotsRemainingInDay, slotsRemainingInWeek;
    
    public void decrement() {
        if(slotsRemainingInShift > 0)
            slotsRemainingInShift--;
        if(slotsRemainingInDay > 0)
            slotsRemainingInDay--;
        if(slotsRemainingInWeek > 0)
            slotsRemainingInWeek--;
    }
}
