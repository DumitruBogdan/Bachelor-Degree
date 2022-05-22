package com.example.activities;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.util.ArrayList;

import classification.Activity;
import classification.Reader;
import util.Parsable;

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        getSupportActionBar().hide();
        new MyTask().execute();
    }

    protected class MyTask extends AsyncTask<Void, Void, ArrayList<Activity>>
    {
        @Override
        protected ArrayList<Activity> doInBackground(Void... voids) {
            ArrayList<Activity> activities = new ArrayList<>();
            try {
                Reader trainReader = new Reader(getResources().openRawResource(R.raw.train_data));
                trainReader.getData(activities);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }
            return activities;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<Activity> activities)
        {
            Intent intent = new Intent(SplashScreen.this, Home.class);
            Parsable.getInstance().setList(activities);
            startActivity(intent);
            finish();
        }
    }
}