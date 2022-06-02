package util;

import java.util.ArrayList;

import classification.Activity;

public class Parsable {
    private static Parsable instance;

    private ArrayList<Activity> list;

    public ArrayList<Activity> getList() {
        return list;
    }

    public void setList(ArrayList<Activity> list) {
        this.list = list;
    }

    private Parsable(){}

    public static Parsable getInstance(){
        if(instance == null){
            instance = new Parsable();
        }
        return instance;
    }
}
