package com.example.activities;


import static com.example.activities.AlertSystem.hasPermissions;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;


import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
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
    private ToggleButton toggleButton;
    private Button phoneNumberButton;
    private TextView phoneNumberText;
    private Queue<String> activitiesCalculated = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        activityText = findViewById(R.id.activityText);
        alertButton = findViewById(R.id.alertSystemButton);
        toggleButton = findViewById(R.id.toggleButton);
        phoneNumberButton = findViewById(R.id.phoneNumberButton);
        phoneNumberText = findViewById(R.id.phoneNumberText);
        accelerometer = new Accelerometer(this);
        gyroscope = new Gyroscope(this);
        activities = Parsable.getInstance().getList();

        toggleButton.setBackgroundColor(Color.RED);
        if (!hasPermissions(Home.this,PERMISSIONS)) {
            ActivityCompat.requestPermissions(Home.this, PERMISSIONS,1);
        }
        readPhoneNumber();
        phoneNumberText.setText(phoneNumber);
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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        activityText.setText("The last activity detected is: \n" + resultedActivity);
                                    }
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

        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, AlertSystem.class);
                startActivity(intent);
            }
        });

        phoneNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                File folder = new File(getFilesDir(), "Resources");
                if (!folder.exists()) {
                    folder.mkdir();
                }
                File file = new File(folder, "phoneNumber.txt");
                try {
                    PrintWriter writer = new PrintWriter(file);
                   phoneNumber = phoneNumberText.getText().toString();
                    writer.append(phoneNumber);
                    writer.flush();
                    writer.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
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
}
