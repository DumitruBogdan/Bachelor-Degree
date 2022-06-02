package com.example.activities;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import classification.Activity;
import io.paperdb.Paper;
import util.Parsable;

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        getSupportActionBar().hide();
        Paper.init(this);
        new MyTask().execute();
    }

    protected class MyTask extends AsyncTask<Void, Void, ArrayList<Activity>> {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected ArrayList<Activity> doInBackground(Void... voids) {
            ArrayList<Activity> activities;
            activities = Paper.book().read("activities");
            if (activities == null) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
                CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, Activity.class);

                try {
                    InputStream asset = getAssets().open("data/data.json");
                    List<Activity> activitiesRead = objectMapper.readValue(asset, collectionType);
                    activities = new ArrayList<>(activitiesRead);
                }
                catch (Exception ioException) {
                    ioException.printStackTrace();
                }
                Paper.book().write("activities", activities);
            }
            return activities;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<Activity> activities) {
            Intent intent = new Intent(SplashScreen.this, Home.class);
            Parsable.getInstance().setList(activities);
            startActivity(intent);
            finish();
        }
    }
}