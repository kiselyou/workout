package run.workout.entities;

import android.location.Location;

public class Point {

    private int bpm;
    private double pace;
    private Long timestamp;
    private Location location;
    private Boolean pause = false;

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return this.location;
    }

    public boolean hasLocation() {
        return this.location != null;
    }

    public void setTimestamp(Long value) {
        this.timestamp = value;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public Boolean isPause() {
        return this.pause;
    }

    public void setPause(Boolean value) {
        this.pause = value;
    }

    public void setBPM(int value) {
        this.bpm = value;
    }

    public int getBPM() {
        return this.bpm;
    }

    public double getPace() {
        return this.pace;
    }

    public void setPace(double pace) {
        this.pace = pace;
    }
}