package sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.ArrayList;

public class Gyroscope implements SensorEventListener {

    private ArrayList<Double> gyroscopeValues = new ArrayList<>();
    private SensorManager manager;
    private Sensor gyroscope;
    Activity foo;

    public Gyroscope(Activity foo) {
        this.foo = foo;
        manager = (SensorManager) this.foo.getSystemService(Context.SENSOR_SERVICE);
        gyroscope = manager.getSensorList(Sensor.TYPE_GYROSCOPE).get(0);
        manager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
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