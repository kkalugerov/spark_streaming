package elasticsearch;

import model.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static utils.JsonUtils.toJson;

public class ElasticConnector {

    public static synchronized void toElastic(List<Document> documents) {
        URL obj;
        HttpURLConnection conn;
        BufferedReader br ;
        try {
            obj = new URL("http://localhost:9200/twitter/test2");

            conn = (HttpURLConnection) obj.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            try (OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream())) {
                out.write(toJson(documents).toString());
            }

            br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            StringBuilder outputBuilder = new StringBuilder();
            while ((output = br.readLine()) != null) {
                outputBuilder.append(output);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
