package com.example.activities;


import static com.example.activities.AlertSystem.hasPermissions;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

import classification.Activity;
import classification.KNN;
import sensors.Accelerometer;
import sensors.Gyroscope;
import util.Parsable;

public class Home extends AppCompatActivity {
    private final String[] PERMISSIONS = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static String phoneNumber;
    private final int MAX_LENGTH = 20;
    private final int POSITIONS = 5;
    private ArrayList<Activity> activities = new ArrayList<>();
    private Accelerometer accelerometer;
    private Gyroscope gyroscope;
    private TextView activityText;
    private Button alertButton;
    private GraphView graph;
    private ToggleButton toggleButton;
    private Queue<String> activitiesCalculated = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        activityText = findViewById(R.id.activityText);
        alertButton = findViewById(R.id.alertSystemButton);
        toggleButton = findViewById(R.id.toggleButton);
        graph = findViewById(R.id.graph);
        accelerometer = new Accelerometer(this);
        gyroscope = new Gyroscope(this);
        activities = Parsable.getInstance().getList();
        toggleButton.setBackgroundColor(Color.RED);
        if (!hasPermissions(Home.this,PERMISSIONS)) {
            ActivityCompat.requestPermissions(Home.this, PERMISSIONS,1);
        }

        Map<String, Integer> hm
                = new HashMap<>();
        hm.put("LAYING", 0);
        hm.put("SITTING", 1);
        hm.put("STANDING", 2);
        hm.put("WALKING", 3);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        series.setColor(Color.BLACK);
        graph.addSeries(series);
        updateGraph();
        readPhoneNumber();
        final Integer[] second = {0};
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!toggleButton.isChecked()) {
                    toggleButton.setBackgroundColor(Color.RED);
                } else {
                    toggleButton.setBackgroundColor(Color.GREEN);
                }
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        boolean flag = false;
                        while (toggleButton.isChecked() && !flag) {
                            try {
                                String resultedActivity = executeTask();
                                runOnUiThread(() -> {
                                    activityText.setText("The last activity detected is: \n" + resultedActivity);
                                    series.appendData(new DataPoint(second[0], hm.get(resultedActivity)), true, 10);
                                    second[0]++;
                                });
                                checkFullList();
                                flag = fallDetection();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        this.cancel();
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 1000);
            }
        });

        alertButton.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, AlertSystem.class);
            startActivity(intent);
        });
    }

    private void savePhoneNumber() {
        File folder = new File(getFilesDir(), "Resources");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File file = new File(folder, "phoneNumber.txt");
        try {
            PrintWriter writer = new PrintWriter(file);
            writer.append(phoneNumber);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String classifiesCurrentValues() {
        ArrayList<Double> query = new ArrayList<>();
        ArrayList<Double> acc = accelerometer.getAccelerometerValues();
        ArrayList<Double> gyro = gyroscope.getGyroscopeValues();
        for (int i = 0; i <= 2; i++) {
            query.add(acc.get(i) / 10);
        }
        for (int i = 0; i <= 2; i++) {
            query.add(gyro.get(i) / 10);
        }
        return KNN.runQuery(activities, query);
    }

    private String executeTask() throws InterruptedException {
        ArrayList<String> lastValues = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            Thread t = new Thread() {
                public void run() {
                    lastValues.add(classifiesCurrentValues());
                }
            };
            t.start();
            Thread.sleep(100);
        }

        Map<String, Long> result = lastValues.stream()
                .collect(groupingBy(Function.identity(),
                        counting()));

        String activityDetermined = new ArrayList<>(result.keySet()).get(0);
        activitiesCalculated.add(activityDetermined);
        return activityDetermined;
    }

    private void checkFullList() {
        if (activitiesCalculated.size() == MAX_LENGTH) {
            activitiesCalculated.remove();
        }
    }

    private boolean fallDetection() {
        if (activitiesCalculated.size() > POSITIONS) {
            ArrayList<String> activities =  new ArrayList<>(activitiesCalculated);
            int lastActivityIndex = activities.size() - 1;
            boolean layingDetection = true;
            for (int i = 0; i <= 4; i++) {
                if (!activities.get(lastActivityIndex - i).equals("LAYING")) {
                    layingDetection = false;
                    break;
                }
            }
            if (!activities.get(lastActivityIndex - POSITIONS).equals("WALKING")) {
                layingDetection = false;
            }
            if (layingDetection) {
                toggleButton.setChecked(false);
                Intent intent = new Intent(Home.this, AlertSystem.class);
                startActivity(intent);
            }
            return layingDetection;
        }
        return false;
    }

    private void readPhoneNumber(){
        String ret = "";
        File folder = new File(getFilesDir(), "Resources");
        File file = new File(folder, "phoneNumber.txt");
        try {
            Scanner myReader = new Scanner(file);
            if (myReader.hasNextLine())
            {
                phoneNumber = myReader.nextLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getPhoneNumber(){
        return phoneNumber;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_phone_forwarded) {
            takePhoneNumber();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void takePhoneNumber() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Write down your new phone number");

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
        input.setHint(phoneNumber);
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String inputText = input.getText().toString();
            String spanner = "^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$";
            if (inputText.matches(spanner)) {
                phoneNumber = inputText;
                savePhoneNumber();
            }
            else {
                AlertDialog alertDialog = new AlertDialog.Builder(Home.this).create();
                alertDialog.setTitle("Warning");
                alertDialog.setMessage("The phone number must have 10 digits!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog1, which1) -> dialog1.dismiss());
                alertDialog.show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateGraph() {
        Viewport vp = graph.getViewport();
        vp.setXAxisBoundsManual(true);
        vp.setYAxisBoundsManual(true);
        vp.setMinY(0);
        vp.setMaxY(3);
        vp.setMinX(0);
        vp.setMaxX(10);
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setVerticalLabels(new String[] {"LAYING", "SITTING", "STANDING", "WALKING"});
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graph.getViewport().setScalable(true);
    }
}
