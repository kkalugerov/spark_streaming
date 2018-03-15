package utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import java.io.*;
import java.util.*;
public class ReadCSV {
    private static List<String> fullFormats = Arrays.asList("yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static List<String> read() {
        List<String> data2 = new ArrayList<>();
        BufferedReader bufferedReader;
        String delimiter = ",";
        String line;
        String[] columns;
        int iteration = 0;
        try {
            bufferedReader = new BufferedReader(new FileReader(
                    new File("/home/zealot/Documents/fixed_Santa_Monika_data.csv")));
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
            writer = new FileWriter(new File("/home/zealot/Documents/fixed.csv"));
            for (String object : objects) {
                writer.write(object);
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String now() {
        return toFullFormat((new DateTime()).getMillis());
    }

    public static String toFullFormat(long dateTime) {
        Iterator var2 = fullFormats.iterator();

        while (var2.hasNext()) {
            String template = (String) var2.next();

            try {
                return format(template, dateTime);
            } catch (IllegalArgumentException var5) {
                ;
            }
        }

        return "";
    }

    public static String format(String template, long millis) {
        return DateTimeFormat.forPattern(template).withZoneUTC().print(millis);
    }

    public static void main(String[] args) throws Exception {

//        save(read());
    }
}
