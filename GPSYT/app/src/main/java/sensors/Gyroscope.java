package sensors;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.ArrayList;

public class Gyroscope implements SensorEventListener {

    private ArrayList<Double> gyroscopeValues = new ArrayList<>();
    private SensorManager manager;
    private Sensor accelerometer;
    Activity foo;

    public Gyroscope(Activity foo) {
        this.foo = foo;
        manager = (SensorManager) this.foo.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        gyroscopeValues.clear();
        for(int i=0 ;i<=2;i++)
            gyroscopeValues.add((double) sensorEvent.values[i]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not in use
    }

    public ArrayList<Double> getGyroscopeValues() {
        return gyroscopeValues;
    }
}