package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadCSV {

    public static List<String> read() {
        Map<String, String> data = new HashMap<>();
        List<String> data2 = new ArrayList<>();
        BufferedReader bufferedReader;
        String delimiter = ";";
        String line;
        String[] columns;
        int iteration = 0;
        try {
            bufferedReader = new BufferedReader(new FileReader(new File("")
                    .getAbsolutePath() + "/src/main/resources/sts_gold_tweet.csv"));
            while ((line = bufferedReader.readLine()) != null) {
                if (iteration == 0) {
                    iteration++;
                    continue;
                } else {
                    columns = line.split(delimiter);
                    data2.add(columns[1] + "   " + columns[2]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data2;
    }

    public static void save(List<String> objects) {
        FileWriter writer;
        try {
            writer = new FileWriter(new File("")
                    .getAbsolutePath() + "/src/main/resources/gold_tweet.txt");
            for (String object : objects) {
                writer.write(object);
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        save(read());
    }
}
