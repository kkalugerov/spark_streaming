package functions;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Time;

public class PersistForEachRDD implements Function2<JavaRDD, Time, Void> {

    public Void call(JavaRDD sourceJavaRDD, Time time) {
        sourceJavaRDD.foreachPartitionAsync(new PersistPartitionFunction());
        return null;
    }
}


