package run.workout.lib;

public class Pace {
    /**
     *
     * @param time the time in seconds
     * @return pace min/km
     */
    public static Float timeToPace(int time) {
        double minutes = Math.floor((double) time / 60);
        double seconds = time % 60;
        return Float.parseFloat(minutes + "." + seconds);
    }

    /**
     *
     * @param time the time in seconds
     * @param distance This is distance in meters
     * @return pace min/km
     */
    public static Float calculatePace(float time, float distance) {
        double minutes = (time / 60) / (distance / 1000);
        int wholeMinutes = (int) Math.floor(minutes);
        int seconds = (int) Math.round((minutes - wholeMinutes) * 60);
        return Float.parseFloat(wholeMinutes + "." + seconds);
    }
}
