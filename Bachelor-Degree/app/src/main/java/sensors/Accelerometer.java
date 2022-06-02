package sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.ArrayList;

public class Accelerometer implements SensorEventListener {

    private ArrayList<Double> accelerometerValues = new ArrayList<>();
    private SensorManager manager;
    private Sensor accelerometer;
    Activity foo;

    public Accelerometer(Activity foo) {
        this.foo = foo;
        manager = (SensorManager) this.foo.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        accelerometerValues.clear();
        for(int i=0 ;i<=2;i++)
            accelerometerValues.add((double) sensorEvent.values[i]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not in use
    }

    public ArrayList<Double> getAccelerometerValues() {
        return accelerometerValues;
    }
}