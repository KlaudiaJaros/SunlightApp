package com.napier.sunlightapp;

/**
 * A class to store information about a walk.
 */
public class Walk {
    private String date;
    private int walkTime;
    private String notes;

    /**
     * Gets the walk date.
     * @return date in a String format
     */
    public String getDate() {
        return date;
    }

    /**
     * Set the walk date.
     * @param date date to be set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Get the walk time in minutes.
     * @return walk time in minutes as an integer
     */
    public int getWalkTime() {
        return walkTime;
    }

    /**
     * Set the walk time.
     * @param walkTime integer time in minutes
     */
    public void setWalkTime(int walkTime) {
        this.walkTime = walkTime;
    }

    /**
     * Get the extra notes about the walk
     * @return String with notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Set the notes
     * @param notes string notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * ToString for the Walk class.
     * @return Walk information for the user in a String format.
     */
    @Override
    public String toString() {
        return "Date: " + date + ", you walked for: " + walkTime + " min\nYour notes: " + notes;
    }
}
