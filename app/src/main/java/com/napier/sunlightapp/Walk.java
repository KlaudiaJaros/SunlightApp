package com.napier.sunlightapp;

public class Walk {
    private String date;
    private int walkTime;
    private String notes;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getWalkTime() {
        return walkTime;
    }

    public void setWalkTime(int walkTime) {
        this.walkTime = walkTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Walk(String date, int walkTime, String notes) {
        this.date = date;
        this.walkTime = walkTime;
        this.notes = notes;
    }

    public Walk() {
    }

    @Override
    public String toString() {
        return "Date: " + date + ", you walked for: " + walkTime + " min\nYour notes: " + notes;
    }
}
