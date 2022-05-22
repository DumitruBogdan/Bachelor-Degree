package classification;
import java.io.IOException;
import java.util.*;

public class KNN {

    private static String findMajorityClass(String[] array) {
        // Convert the input array into a set to store only unique values
        Set<String> auxSet = new HashSet<>(Arrays.asList(array));
        // Now we convert it back to an array of strings
        String[] uniqueValues = auxSet.toArray(new String[0]);
        int[] frequencyOfActivities = new int[uniqueValues.length];
        // Loop through unique strings and count how many times they appear in the original array
        for (int i = 0; i < uniqueValues.length; i++) {
            for (String s : array) {
                if (s.equals(uniqueValues[i])) {
                    frequencyOfActivities[i]++;
                }
            }
        }
        // Find the max occurrences of activity/ activities
        int max = frequencyOfActivities[0];
        for (int counter = 1; counter < frequencyOfActivities.length; counter++) {
            if (frequencyOfActivities[counter] > max) {
                max = frequencyOfActivities[counter];
            }
        }
        // How many times max appears, it's known that max will appear at least once in frequencyOfActivities
        int freq = 0;
        for (int frequencyOfActivity : frequencyOfActivities) {
            if (frequencyOfActivity == max) {
                freq++;
            }
        }
        // Index of most freq value if we have only one mode
        int index = -1;
        if (freq == 1) {
            for (int counter = 0; counter < frequencyOfActivities.length; counter++) {
                if (frequencyOfActivities[counter] == max) {
                    index = counter;
                    break;
                }
            }
            return uniqueValues[index];
        } else {
            int[] mostFrequentActivitiesIndexes = new int[freq];
            int indexOfFAI = 0;
            for (int counter = 0; counter < frequencyOfActivities.length; counter++) {
                if (frequencyOfActivities[counter] == max) {
                    // Save index of each max count value
                    mostFrequentActivitiesIndexes[indexOfFAI] = counter;
                    // Increase index of mostFrequentActivitiesIndexes array
                    indexOfFAI++;
                }
            }
            Random generator = new Random();
            // Get random number 0 <= randomIndex < size of mostFrequentActivitiesIndexes
            int randomIndex = generator.nextInt(mostFrequentActivitiesIndexes.length);
            //System.out.println("random index: " + randomIndex);
            return uniqueValues[mostFrequentActivitiesIndexes[randomIndex]];
        }
    }

    public static void algorithmEfficiency(ArrayList<Activity> trainActivities) throws IOException {
//        Reader testReader = new Reader("./src/main/resources/testData.xlsx");
//        ArrayList<Activity> testActivities = new ArrayList<>();
//        testReader.getData(testActivities);
//        int numberOfCorrectPredictedActivities = 0;
//        for (Activity testActivity : testActivities) {
//            String resultedActivity = runQuery(trainActivities, testActivity.getValues());
//            if (resultedActivity.equals(testActivity.getActivityName())){
//                numberOfCorrectPredictedActivities++;
//            }
//        }
//        double rate = ((double) numberOfCorrectPredictedActivities/testReader.getLastRowIndex()) * 100;
//        System.out.println("The algorithm precision is: " + rate);
    }

    public static String runQuery(ArrayList<Activity> activities, ArrayList<Double> data){
        int k = 21;// # of neighbours
        // List to save the k most probably results
        List<Result> resultList = new ArrayList<>();
        // Calculate the sensor data differences
        for (Activity activity : activities) {
            double dist = 0.0;
            for (int j = 0; j < activity.getValues().size(); j++) {
                dist += Math.pow(activity.getValues().get(j) - data.get(j), 2);
            }
            double distance = Math.sqrt(dist);
            resultList.add(new Result(distance, activity.getName()));
        }
        resultList.sort(new DistanceComparator());
        String[] ss = new String[k];
        for (int x = 0; x < k; x++) {
            //System.out.println(resultList.get(x).activityName + " .... " + resultList.get(x).distance);
            // Get the activity name of k nearest instances (activities) from the list into an array
            ss[x] = resultList.get(x).activityName;
        }
        return findMajorityClass(ss);
    }
}
