package com.deliveryhero.models;

import java.util.List;

public class Rider {
    private String id;
    private List<Shift> shifts;

    public Rider(String id, List<Shift> shifts){
        this.id = id;
        this.shifts = shifts;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public List<Shift> getShifts(){
        return shifts;
    }

    public void setShifts(List<Shift> shifts){
        this.shifts = shifts;
    }
}
