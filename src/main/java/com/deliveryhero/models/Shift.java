package com.deliveryhero.models;

import java.util.Date;

public class Shift {
    private String id;
    private Date start;
    private Date end;

    public Shift(String id, Date start, Date end){
        this.id = id;
        this.start = start;
        this.end = end;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public Date getStart(){
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd(){
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}
