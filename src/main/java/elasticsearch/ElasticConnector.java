package elasticsearch;

import analytics.CoreNLP;
import model.Document;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static utils.JsonUtils.toJson;

public class ElasticConnector implements Serializable {
    private static Properties properties = new Properties();
    private static int port;
    private static String host;
    private static ElasticConnector INSTANCE;

    private static void loadProps() {
        InputStream inputStream = CoreNLP.class.getClassLoader().getResourceAsStream("elastic_search.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void init() {
        host = properties.getProperty("elastic.host");
        port = Integer.parseInt(properties.getProperty("elastic.port"));
    }

    public static ElasticConnector getInstance() {
        try {
            loadProps();
            init();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        if (INSTANCE == null) {
            synchronized (ElasticConnector.class) {
                INSTANCE = new ElasticConnector();
            }
        }
        return INSTANCE;
    }

    public static synchronized void toElastic(List<Document> documents) {
        URL obj;
        HttpURLConnection conn;
        BufferedReader br;
        try {
            obj = new URL("http://"+ host +":" + port + "/twitter/test2");

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
