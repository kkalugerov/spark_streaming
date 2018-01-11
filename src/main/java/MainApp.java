import model.Document;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import receivers.MainReceiver;
import receivers.ReceiverFactory;
import streams.Stream;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MainApp {
    public static Logger logger = LogManager.getLogger(MainApp.class);

    public static Properties drainProp = new Properties();
    public static JavaStreamingContext javaStreamingContext;
    public static SparkConf sparkConf;

    private static List<MainReceiver<Document>> receivers = new ArrayList<>();
    private static String[] streams;

    public static void main(String[] args) throws IOException {
//        sparkConf = new SparkConf().setMaster("local[2]").setAppName("SparkTwitterHelloWorldExample");
//        logger.info("args: " + args);

        init(args);

//        long durantion = Long.parseLong(drainProp.getProperty("drain.delay"));
        javaStreamingContext = new JavaStreamingContext(sparkConf, Durations.milliseconds(2000));
        receivers.addAll(ReceiverFactory.getReceivers(streams, drainProp));

        Stream stream = new Stream(javaStreamingContext, receivers);
        stream.startAndAwait();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            javaStreamingContext.stop(true, true);
//            logger.info("Spark stream was stopped gracefully!");
        }));
    }

    private static void init(String[] args) throws IOException {


        if (args.length < 2)
            throw new IllegalArgumentException("Please use at least two arguments: <app_name> <stream_name1,stream_name2,...,stream_nameN>. \n <stream_name> can be twitter,facebook etc");

        initSparkConf(args[0]);
        streams = args[1].split(",");
        logger.info("Streams : " + streams);


        String propsLocation = "";
        if (args.length > 2)
            propsLocation = args[2];

        logger.info("Loading properties from " + propsLocation);
        loadProp(propsLocation);
        logger.info("Properties loaded!");
        loadLogging();
    }
//
    private static void initSparkConf(String master) {

        sparkConf = new SparkConf().setAppName(master);
        if (master.contains("local"))
            sparkConf.setMaster("local[*]");

    }

    private static void loadLogging() {
        Boolean isEmbedded = Boolean.valueOf(drainProp.getProperty("drain.log4j.is_embedded"));
        if (!isEmbedded) {
            String log4jConfigFile = drainProp.getProperty("drain.log4j.location");
            ConfigurationSource source = null;
            try {
                source = new ConfigurationSource(new FileInputStream(log4jConfigFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Configurator.initialize(null, source);
        } else {
            logger = LogManager.getLogger(MainApp.class);
        }
        logger.info("Logging loaded: \n" + logger.getName() + "\n" + logger.getLevel() + "\n" + logger);
    }

    private static void loadProp(String propLocation) throws IOException {

        if (propLocation.contains("database")) {
            return;
        }

        if (propLocation.isEmpty())
            drainProp.load(MainApp.class.getClassLoader().getResourceAsStream("drain.properties"));
        else drainProp.load(new FileInputStream(propLocation));

        Arrays.asList("twitter")
                .forEach(entry -> drainProp.putAll(getProperties(entry)));
    }

    private static Properties getProperties(String entry) {
        Properties properties = new Properties();
        try {
            if (Boolean.valueOf(drainProp.getProperty(String.format("drain.%s.is_embedded", entry))))
                properties.load(MainApp.class.getClassLoader().getResourceAsStream(String.format("%s.properties", entry)));
            else properties.load(new FileInputStream(drainProp.getProperty(String.format("drain.%s.location", entry))));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return properties;
    }


}
