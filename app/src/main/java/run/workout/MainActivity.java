package run.workout;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import run.workout.entities.Point;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    public static final int PERMISSIONS_REQUEST_LOCATION = 1;

    Button btnStart;
    Button btnPause;
    Button btnStop;
    Button btnContinue;

    ImageButton btnGPSEnabled;
    ImageButton btnGPSDisabled;

    ImageButton btnProviderNetwork;
    ImageButton btnProviderUnknown;

    private TextView workoutTime;
    private TextView workoutPace;
    private TextView workoutAVGPace;
    private TextView workoutBPM;
    private TextView workoutDistance;

    private TextView accuracyTest;

    private LinearLayout layoutStart;
    private LinearLayout layoutPause;
    private LinearLayout layoutStop;

    private Workout workout = new Workout();

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.button_start);
        btnPause = findViewById(R.id.button_pause);
        btnStop = findViewById(R.id.button_stop);
        btnContinue = findViewById(R.id.button_continue);
        btnGPSEnabled = findViewById(R.id.button_gps_enabled);
        btnGPSDisabled = findViewById(R.id.button_gps_disabled);

        btnProviderNetwork = findViewById(R.id.button_provider_network);
        btnProviderUnknown = findViewById(R.id.button_provider_unknown);

        workoutTime = findViewById(R.id.workout_time);
        workoutPace = findViewById(R.id.workout_pace);
        workoutAVGPace = findViewById(R.id.workout_avg_pace);
        workoutBPM = findViewById(R.id.workout_bpm);
        workoutDistance = findViewById(R.id.workout_distance);

        layoutStart = findViewById(R.id.layout_start);
        layoutPause = findViewById(R.id.layout_pause);
        layoutStop = findViewById(R.id.layout_stop);

        accuracyTest = findViewById(R.id.accuracyTest);

//        btnStart.setOnLongClickListener(this);
//        btnPause.setOnLongClickListener(this);
//        btnStop.setOnLongClickListener(this);
//        btnContinue.setOnLongClickListener(this);
        btnStart.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnContinue.setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Point point1 = new Point();
        Log.d("debug", "p1: " + point1.getId());
        Point point2 = new Point();
        Log.d("debug", "p2: " + point2.getId());
        Point point3 = new Point();
        Log.d("debug", "p3: " + point3.getId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                changeGPSStatus(false);
                showAlertAllowLocation();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
                changeGPSStatus(false);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, locationListener);
            changeGPSStatus(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);

        workout.stop();
        workout = new Workout();
    }

    private LocationListener locationListener = new LocationListener() {
        boolean enabled = false;

        @Override
        public void onLocationChanged(Location location) {
            if (!enabled) {
                enabled = true;
                return;
            }

            workout.setLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Boolean newProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            changeGPSStatus(gpsProvider || newProvider);
            if (!gpsProvider && !newProvider) {
                btnProviderNetwork.setVisibility(ImageButton.INVISIBLE);
                btnProviderUnknown.setVisibility(ImageButton.INVISIBLE);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            changeGPSStatus(true);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                btnProviderNetwork.setVisibility(ImageButton.VISIBLE);
                btnProviderUnknown.setVisibility(ImageButton.INVISIBLE);
            } else if (!provider.equals(LocationManager.GPS_PROVIDER)) {
                btnProviderUnknown.setVisibility(ImageButton.VISIBLE);
                btnProviderNetwork.setVisibility(ImageButton.INVISIBLE);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i("debug", "permission was granted, yay! Do the");
                } else {
                    Log.i("debug", "permission denied, boo! Disable the");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void startPauseAnimation() {
        Animation sunRiseAnimation = AnimationUtils.loadAnimation(this, R.anim.activity_pause);
        workoutTime.startAnimation(sunRiseAnimation);
    }

    public void stopPauseAnimation() {
        workoutTime.clearAnimation();
    }

    public void updateView() {
        workoutTime.setText(workout.timeAsString());
        workoutPace.setText(workout.getPace());
        workoutAVGPace.setText(workout.getAVGPace());
        workoutBPM.setText(workout.getBPM());
        workoutDistance.setText(workout.getDistance());
        accuracyTest.setText(workout.getAccuracy());
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start:
                workout.start();
                workout.setPointEventListener(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateView();
                            }
                        });
                    }
                });
                // Была нажата кнопка старт. Запустить таймер и поменять экран с кноками.
                layoutStart.setVisibility(View.INVISIBLE);
                layoutPause.setVisibility(View.VISIBLE);
                break;
            case R.id.button_pause:
                workout.pause();

                // Была нажата кнопка пауза. Добавить метку в таймере и поменять экран с кноками.
                startPauseAnimation();
                layoutPause.setVisibility(View.INVISIBLE);
                layoutStop.setVisibility(View.VISIBLE);
                break;
            case R.id.button_stop:
                // Остоновить тренировку
                workout.stop();

                // Поменять экран с кноками, анимацией.
                stopPauseAnimation();
                layoutStop.setVisibility(View.INVISIBLE);
                layoutStart.setVisibility(View.VISIBLE);

                // Сохранить данные.
                Toast toast = Toast.makeText(getApplicationContext(), "Тренировка сохранена", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.show();

                // Сбросить тренироку в дефолтное состояние.
                workout = new Workout();
                updateView();
                break;
            case R.id.button_continue:
                workout.next();

                // Продолжить тренировку. Вернуть экран кнопок тренировки
                stopPauseAnimation();
                layoutStop.setVisibility(View.INVISIBLE);
                layoutPause.setVisibility(View.VISIBLE);
                break;
        }
    }

    void changeGPSStatus(Boolean status) {
        if (status) {
            btnGPSEnabled.setVisibility(ImageButton.VISIBLE);
            btnGPSDisabled.setVisibility(ImageButton.INVISIBLE);
        } else {
            btnGPSEnabled.setVisibility(ImageButton.INVISIBLE);
            btnGPSDisabled.setVisibility(ImageButton.VISIBLE);
        }
    }

    void showAlertAllowLocation() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setMessage("Чтобы улучшить работу приложения, разрешить приложению использовать местоположение.");
        adb.setCancelable(false);
        adb.setPositiveButton("Разрешить", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
            }
        });
        adb.setNegativeButton("Нет, Спасибо", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        adb.create();
        adb.show();
    }
}
