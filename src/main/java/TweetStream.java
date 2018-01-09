import org.apache.spark.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.*;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.twitter.*;
import twitter4j.GeoLocation;
import twitter4j.Status;

public class TweetStream {
    public static void main(String[] args) {
        String consumerKey = "XwVbsmDZ19K8cSEt0yH9EQMh5";
        String consumerSecret = "7sfpa97aBpA1J9mOoyaTHC96Z5YvJImGNoOI8OZa2IjjhXe9gu";
        String accessToken = "3421080364-5Gjqf8BP7yNB0MOXTw8aOE5AlNXhl34PPDS3twy";
        String accessTokenSecret = "p7RL3FjuCk9U4OpoU3lSBZYqyNXm4m24pF8iZN816d3qp";

        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("SparkTwitterHelloWorldExample");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, new Duration(2000));

        System.setProperty("twitter4j.oauth.consumerKey", consumerKey);
        System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret);
        System.setProperty("twitter4j.oauth.accessToken", accessToken);
        System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret);

        JavaReceiverInputDStream<Status> twitterStream = TwitterUtils.createStream(jssc);

        // Without filter: Output text of all tweets
//        JavaDStream<String> statuses = twitterStream.map(
//                new Function<Status, String>() {
//                    public String call(Status status) { return status.getText(); }
//                }
//        );

        // With filter: Only use tweets with geolocation and print location+text.

        JavaDStream<Status> tweetsWithLocation = twitterStream.filter(
                new Function<Status, Boolean>() {
                    public Boolean call(Status status){
                        if (status.getText() != null) {
                            System.out.println(status.getText());
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
        );
        JavaDStream<String> statuses = tweetsWithLocation.map(
                new Function<Status, String>() {
                    public String call(Status status) {
                        return status.getText().toString() + ": " + status.getText();
                    }
                }
        );

        statuses.print();
        jssc.start();
    }
}
