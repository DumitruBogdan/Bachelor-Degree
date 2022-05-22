package classification;

import java.util.Comparator;

public class Result {
    double distance;
    String activityName;
    public Result(double distance, String activityName){
        this.activityName = activityName;
        this.distance = distance;
    }
}

class DistanceComparator implements Comparator<Result> {
    @Override
    public int compare(Result result1, Result result2) {
        return Double.compare(result1.distance, result2.distance);
    }
}

