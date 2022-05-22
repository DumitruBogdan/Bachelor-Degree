package classification;

import androidx.annotation.NonNull;

import java.util.ArrayList;
public class Activity {
    private String name;
    private ArrayList<Double> values;

    public Activity(String activityName, ArrayList<Double> values) {
        this.name = activityName;
        this.values = values;
    }

    public ArrayList<Double> getValues() {
        return values;
    }

    public String getName() {
        return name;
    }


    @NonNull
    @Override
    public String toString() {
        return name + "    " + values;
    }
}
