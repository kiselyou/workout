package run.workout;

import android.location.Location;
import android.util.Log;

import run.workout.lib.Pace;
import run.workout.entities.Point;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;


class Workout extends TimerTask {
    /**
     * Interval in milliseconds.
     */
    private static final int TIMER_INTERVAL = 1000;

    private static final int DESIRED_ACCURACY = 12;

    /**
     * Count points to calculate current pace.
     */
    private static final int PACE_COUNT = 20;

    /**
     * Time in milliseconds.
     */
    private int time = 0;

    /**
     * Distance in meters.
     */
    private float distance = 0;
    private Runnable eventListener;
    private Timer timer = new Timer();
    private Boolean startActivity = false;
    private Boolean pauseActivity = false;
    private SimpleDateFormat formatter;
    private List<Point> points = new ArrayList<Point>();

    private int cachePaceCount = 0;
    private double cachePaceSum = 0;

    private int testCountLocation = 0;

    Workout() {
        this.formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        this.formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Найти последнюю активную точку (которая не на паузе) и в которой есть локация с подходящей точностью.
     *
     * @param index - числовое значение которое говорит какую активную точку с конца брать.
     *              Например: findSatisfiedPoint(1) - Взять первую активную точку с конца массива.
     *                        findSatisfiedPoint(2) - Взять вторую активную точку с конца массива.
     * @return Point
     */
    private Point findSatisfiedPoint(int index) {
        int counterIndex = 0;
        int size = this.points.size() - 1;
        for (int i = size; i >= 0; i--) {
            Point point = this.points.get(i);
            if (!this.isPointSatisfied(point)) {
                continue;
            }

            counterIndex++;
            if (counterIndex == index) {
                return point;
            }
        }
        return null;
    }

    /**
     * Найти активную точку которая следовала перед точкой переданной в параметре.
     *
     * @param point - Точка для которой найти предыдущую активную точку.
     * @return Point
     */
    private Point findSatisfiedPrevPoint(Point point) {
        boolean isMatched = false;
        int size = this.points.size() - 1;
        for (int i = size; i >= 0; i--) {
            Point matchPoint = this.points.get(i);
            if (!isMatched) {
                if (matchPoint.getId() == point.getId()) {
                    isMatched = true;
                }
            } else {
                if (this.isPointSatisfied(matchPoint)) {
                    return matchPoint;
                }
            }
        }
        return null;
    }

    /**
     * Получение последней точки. Не зависимо от статуса.
     *
     * @return Point|null
     */
    private Point getLastPoint() {
        if (this.points.size() == 0) {
            return null;
        }

        return this.points.get(this.points.size() - 1);
    }

    String getAccuracy() {
        Point point = this.findSatisfiedPoint(1);
        if (point == null) {
            return null;
        }
        if (!point.hasLocation()) {
            return null;
        }
        Location location = point.getLocation();
        if (!location.hasAccuracy()) {
            return  null;
        }
        return "Accuracy: " + location.getAccuracy() + " Points: " + this.points.size() + " countLocation: " + testCountLocation + " countDistance: " + this.cachePaceCount;
    }

    String getDistance() {
        return String.format("%.3f", this.distance / 1000);
    }

    void setLocation(Location location) {
        Point lastPoint = this.getLastPoint();
        if (lastPoint == null) {
            return;
        }

        this.testCountLocation++;
        lastPoint.setLocation(location);

        if (this.pauseActivity) {
            return;
        }

        if (!this.isPointSatisfied(lastPoint)) {
            return;
        }

        // Предпоследняя точка
        Point prevPoint = this.findSatisfiedPoint(2);
        if (prevPoint == null) {
            return;
        }

        // Расчет растояния и темпа между последней и предпоследней точками.
        Location prevLocation = prevPoint.getLocation();
        Location currLocation = lastPoint.getLocation();

        float time = lastPoint.getTimestamp() - prevPoint.getTimestamp();
        float distance = prevLocation.distanceTo(currLocation);
        double pace = Pace.calculatePace(time / 1000, distance);

        this.cachePaceCount++;
        this.cachePaceSum += pace;
        this.distance += distance;
        this.eventListener.run();
    }

    /**
     *
     * @return int - средний темп всех точек.
     */
    String getAVGPace() {
        if (this.cachePaceCount == 0) {
            return Pace.format(0);
        }

        return Pace.format(this.cachePaceSum / this.cachePaceCount);
    }

    /**
     *
     * @return - средний темп за последние N точек.
     */
    String getPace() {
        int size = this.points.size();
        if (size == 0) {
            return Pace.format(0);
        }

        double sum = 0;
        int count = 0;
        for (int i = size - 1; i >= 0; i--) {
            Point point = this.points.get(i);
            if (!this.isPointSatisfied(point)) {
                continue;
            }

            Point prevPoint = findSatisfiedPrevPoint(point);
            if (prevPoint == null) {
                continue;
            }

            // Расчет растояния и темпа между последней и предпоследней точками.
            Location prevLocation = prevPoint.getLocation();
            Location currLocation = point.getLocation();

            float time = point.getTimestamp() - prevPoint.getTimestamp();
            float distance = prevLocation.distanceTo(currLocation);
            double pace = Pace.calculatePace(time / 1000, distance);

            count++;
            sum += pace;
            if (count >= Workout.PACE_COUNT) {
                break;
            }
        }

        return Pace.format(sum / count);
    }

    private boolean isPointSatisfied(Point point) {
        if (point.isPause()) {
            return false;
        }

        if (!point.hasLocation()) {
            return false;
        }

        Location location = point.getLocation();
        if (!location.hasAccuracy()) {
            return false;
        }

        return location.getAccuracy() <= Workout.DESIRED_ACCURACY;
    }

    String getBPM() {
        Point point = this.getLastPoint();
        if (point == null) {
            return "0";
        }
        return  "" + point.getBPM();
    }

    /**
     * Время активности в виде строки: HH:mm:ss
     *
     * @return String|?
     */
    String timeAsString() {
        if (this.time > 0) {
            return this.formatter.format(new Date(this.time));
        }
        return this.formatter.format(new Date(0));
    }

    @Override
    public void run() {
        if (!this.startActivity) {
            return;
        }
        if (!this.pauseActivity) {
            this.time += TIMER_INTERVAL;
        }
        Point point = new Point();
        point.setPause(this.pauseActivity);
        point.setTimestamp(System.currentTimeMillis());
        this.points.add(point);
        this.eventListener.run();
    }

    /**
     *
     * @param action это действие будет выполняться прикаждом тике таймера при каждом добавлении местоположения пользователя.
     */
    void setPointEventListener(Runnable action) {
        this.eventListener = action;
    }

    /**
     * Начать тренировку.
     */
    void start() {
        this.startActivity = true;
        timer.scheduleAtFixedRate(this, TIMER_INTERVAL, TIMER_INTERVAL);
    }

    /**
     * Поставить тренировку на паузу.
     */
    void pause() {
        this.pauseActivity = true;
    }

    /**
     * Продолжить тренировку после паузы.
     */
    void next() {
        this.pauseActivity = false;
    }

    /**
     * Полная остоновка таймера.
     */
    void stop() {
        this.startActivity = false;
        this.pauseActivity = false;
        timer.cancel();
        timer.purge();
    }
}