package com.example.mobilalkfejl;

public class Reservation {
    private String id;
    private String startTimestamp;
    private int duration;
    private int tableCount;
    private boolean wholePlace;
    private String phone;

    public Reservation() {

    }

    public Reservation(String id, String startTimestamp, int duration, int tableCount, boolean wholePlace, String phone) {
        this.id = id;
        this.startTimestamp = startTimestamp;
        this.duration = duration;
        this.tableCount = tableCount;
        this.wholePlace = wholePlace;
        this.phone = phone;
    }

    // Getterek és setterek
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStartTimestamp() { return startTimestamp; }
    public void setStartTimestamp(String startTimestamp) { this.startTimestamp = startTimestamp; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getTableCount() { return tableCount; }
    public void setTableCount(int tableCount) { this.tableCount = tableCount; }

    public boolean isWholePlace() { return wholePlace; }
    public void setWholePlace(boolean wholePlace) { this.wholePlace = wholePlace; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
