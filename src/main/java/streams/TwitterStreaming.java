package streams;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.TwitterUtils;
import scala.Tuple2;
import twitter4j.FilterQuery;
import twitter4j.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Displays the most positive hash tags by joining the streaming Twitter data with a static RDD of
 * the AFINN word list (http://neuro.imm.dtu.dk/wiki/AFINN)
 */
public class TwitterStreaming {

    public static List<Long> userIds = Arrays.asList(2312333412L, 14338147L, 295218901L, 745758443324080128L, 797826112810315776L,
            2799211554L, 218996805L, 176758255L, 854000068357234688L, 852256178021294080L, 2873536332L, 947920374783766528L,
            33962758L, 1333467482L, 213236426L, 14379660L, 125304737L, 22682896L, 2362854624L, 3129477561L, 386728215L,
            746779366550544384L, 2568108282L, 774689518767181828L, 385562752L, 143053926L, 2349043879L, 866768940239814659L,
            2313671966L, 2478439963L, 734688391942524928L, 917195081408319488L, 928647522817417216L, 574032254L);

    private static List<String> consumerKeys = Arrays.asList("EIID1RQVxlNhCsWeHMUbg","XwVbsmDZ19K8cSEt0yH9EQMh5");
    private static List<String> consumerSecrets = Arrays.asList("WWQWBRyw1hB7t0NhsiOYXJJRdUViUNlo4nKywDKQ","7sfpa97aBpA1J9mOoyaTHC96Z5YvJImGNoOI8OZa2IjjhXe9gu");
    private static List<String> accessTokens = Arrays.asList("106389387-AzUR0xg1hKykLV4tP74XyMjHhA2mdekXsY1ZW0qL","3421080364-5Gjqf8BP7yNB0MOXTw8aOE5AlNXhl34PPDS3twy");
    private static List<String> accessSecrets = Arrays.asList("dylzPdDnDzuHk1DwHoh1VWKqFo6VJSaWrquXF3udmfrfQ","p7RL3FjuCk9U4OpoU3lSBZYqyNXm4m24pF8iZN816d3qp");

    public static List<String> mentions(String text) {
        String[] words = text.split(" ");
        List<String> mentions = new ArrayList<>();
        for (String word : words) {
            if (word.startsWith("@")) {
                String mention = word;
                mentions.add(mention);
                return mentions;
            }
        }
        return new ArrayList<>();
    }


    public static void main(String[] args) {

        if (args.length < 4) {
            System.err.println("Usage: JavaTwitterHashTagJoinSentiments <consumer key>" +
                    " <consumer secret> <access token> <access token secret> [<filters>]");
            System.exit(1);
        }

        //StreamingExamples.setStreamingLogLevels();
        // Set logging level if log4j not configured (override by adding log4j.properties to classpath)
        if (!Logger.getRootLogger().getAllAppenders().hasMoreElements()) {
            Logger.getRootLogger().setLevel(Level.WARN);
        }

        String consumerKey = args[0];
        String consumerSecret = args[1];
        String accessToken = args[2];
        String accessTokenSecret = args[3];
        String[] query = {};
//                long[] filters = Arrays.copyOfRange(userIds, 0, args.length);


        // Set the system properties so that Twitter4j library used by Twitter stream
        // can use them to generate OAuth credentials
        JavaStreamingContext jssc = null;
        JavaSparkContext javaSparkContext = null;
        for(int i= 0;i<consumerKeys.size();i++) {
            System.setProperty("twitter4j.oauth.consumerKey", consumerKeys.get(i));
            System.setProperty("twitter4j.oauth.consumerSecret", consumerSecrets.get(i));
            System.setProperty("twitter4j.oauth.accessToken", accessTokens.get(i));
            System.setProperty("twitter4j.oauth.accessTokenSecret", accessSecrets.get(i));


//        System.setProperty("twitter4j.oauth.consumerKey", args[0]);
//        System.setProperty("twitter4j.oauth.consumerSecret", args[1]);
//        System.setProperty("twitter4j.oauth.accessToken", args[2]);
//        System.setProperty("twitter4j.oauth.accessTokenSecret", args[3]);
            SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("TwitterStreamin");

            jssc = new JavaStreamingContext(sparkConf.set("spark.driver.allowMultipleContexts", "true"), new Duration(1000));
            javaSparkContext = jssc.sparkContext();


            JavaReceiverInputDStream<Status> stream = TwitterUtils.createStream(jssc, new String[]{"Monero", "BitCoin"});


            JavaDStream<String> words = stream.flatMap((FlatMapFunction<Status, String>) s -> {
                System.out.println(s.getText());
                return mentions(s.getText());
            });


            words.print();


//
//
//                JavaDStream<String> hashTags = words.filter(new Function<String, Boolean>() {
//                    @Override
//                    public Boolean call(String word) {
//                        return word.startsWith("#");
//                    }
//                });
//
//                JavaDStream<String> mentions = words.filter(new Function<String, Boolean>() {
//                    @Override
//                    public Boolean call(String word) throws Exception {
//                        return word.startsWith("@");
//                    }
//                });

            // Read in the word-sentiment list and create a static RDD from it
//                String wordSentimentFilePath = "data/AFINN-111.txt";
//                final JavaPairRDD<String, Double> wordSentiments = jssc.sparkContext()
//                        .textFile(wordSentimentFilePath)
//                        .mapToPair(new PairFunction<String, String, Double>() {
//                            @Override
//                            public Tuple2<String, Double> call(String line) {
//                                String[] columns = line.split("\t");
//                                return new Tuple2<>(columns[0], Double.parseDouble(columns[1]));
//                            }
//                        });
//
//                JavaPairDStream<String,Integer> mentionsCount = mentions.mapToPair(
//                        new PairFunction<String, String, Integer>() {
//                            @Override
//                            public Tuple2<String, Integer> call(String s) throws Exception {
//                                return new Tuple2<>(s.substring(1),1);
//                            }
//                        });
//
//                JavaPairDStream<String, Integer> hashTagCount = hashTags.mapToPair(
//                        new PairFunction<String, String, Integer>() {
//                            @Override
//                            public Tuple2<String, Integer> call(String s) {
//                                // leave out the # character
//                                return new Tuple2<>(s.substring(1), 1);
//                            }
//                        });
//
//                JavaPairDStream<String,Integer> mentionsTotal = mentionsCount.reduceByKeyAndWindow(
//                        new Function2<Integer, Integer, Integer>() {
//                            @Override
//                            public Integer call(Integer a, Integer b) throws Exception {
//                                return a + b;
//                            }
//                        }, new Duration(10000));
//
//
//
//                JavaPairDStream<String, Integer> hashTagTotals = hashTagCount.reduceByKeyAndWindow(
//                        new Function2<Integer, Integer, Integer>() {
//                            @Override
//                            public Integer call(Integer a, Integer b) {
//                                return a + b;
//                            }
//                        }, new Duration(10000));
//
//                // Determine the hash tags with the highest sentiment values by joining the streaming RDD
//                // with the static RDD inside the transform() method and then multiplying
//                // the frequency of the hash tag by its sentiment value
//                JavaPairDStream<String, Tuple2<Double, Integer>> joinedTuples =
//                        hashTagTotals.transformToPair(new Function<JavaPairRDD<String, Integer>,
//                                JavaPairRDD<String, Tuple2<Double, Integer>>>() {
//                            @Override
//                            public JavaPairRDD<String, Tuple2<Double, Integer>> call(
//                                    JavaPairRDD<String, Integer> topicCount) {
//                                return wordSentiments.join(topicCount);
//                            }
//                        });
//
//                JavaPairDStream<String, Double> topicHappiness = joinedTuples.mapToPair(
//                        new PairFunction<Tuple2<String, Tuple2<Double, Integer>>, String, Double>() {
//                            @Override
//                            public Tuple2<String, Double> call(Tuple2<String,
//                                    Tuple2<Double, Integer>> topicAndTuplePair) {
//                                Tuple2<Double, Integer> happinessAndCount = topicAndTuplePair._2();
//                                return new Tuple2<>(topicAndTuplePair._1(),
//                                        happinessAndCount._1() * happinessAndCount._2());
//                            }
//                        });
//
//                JavaPairDStream<Double, String> happinessTopicPairs = topicHappiness.mapToPair(
//                        new PairFunction<Tuple2<String, Double>, Double, String>() {
//                            @Override
//                            public Tuple2<Double, String> call(Tuple2<String, Double> topicHappiness) {
//                                return new Tuple2<>(topicHappiness._2(),
//                                        topicHappiness._1());
//                            }
//                        });
//
//                JavaPairDStream<Double, String> happiest10 = happinessTopicPairs.transformToPair(
//                        new Function<JavaPairRDD<Double, String>, JavaPairRDD<Double, String>>() {
//                            @Override
//                            public JavaPairRDD<Double, String> call(
//                                    JavaPairRDD<Double, String> happinessAndTopics) {
//                                return happinessAndTopics.sortByKey(false);
//                            }
//                        }
//                );
//
//                // Print hash tags with the most positive sentiment values
//                happiest10.foreachRDD(new Function<JavaPairRDD<Double,String>, Void>() {
//                    @Override
//                    public Void call(JavaPairRDD<Double, String> happinessTopicPairs) {
//                        List<Tuple2<Double, String>> topList = happinessTopicPairs.take(10);
//                        System.out.println(
//                                String.format("\nHappiest topics in last 10 seconds (%s total):",
//                                        happinessTopicPairs.count()));
//                        for (Tuple2<Double, String> pair : topList) {
//                            System.out.println(
//                                    String.format("%s (%s happiness)", pair._2(), pair._1()));
//                        }
//                        return null;
//                    }
//                });

        }
            javaSparkContext.sc().startTime();
            jssc.start();
            jssc.awaitTermination();

    }
}
