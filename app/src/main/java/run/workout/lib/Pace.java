package run.workout.lib;

import android.util.Log;

public class Pace {
    /**
     *
     * @param minutes the time in seconds
     * @return pace min/km
     */
    public static String format(double minutes) {
        int wholeMinutes = (int) Math.floor(minutes);
        int seconds = (int) Math.round((minutes - wholeMinutes) * 60);
        return wholeMinutes + "' " + seconds + "''";
    }

    /**
     *
     * @param seconds the time in seconds
     * @param distance This is distance in meters
     * @return pace min/km
     */
    public static double calculatePace(float seconds, float distance) {
        return (seconds / 60) / (distance / 1000);
    }
}
