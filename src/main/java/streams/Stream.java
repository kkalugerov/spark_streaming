package streams;

import model.Model;
import functions.PersistForEachRDD;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import receivers.MainReceiver;

import java.util.List;

public class Stream {
    private JavaDStream stream;
    private JavaStreamingContext context;

    public Stream(JavaStreamingContext context) {
        this.context = context;
    }

    public Stream(JavaStreamingContext context, List<MainReceiver<Model>> receiverList) {
        this(context);

        if (receiverList != null) receiverList.forEach(this::addReceiverToStream);

    }

    private void addReceiverToStream(MainReceiver receiver) {
        if (stream == null) stream = context.receiverStream(receiver);
        else stream = stream.union(context.receiverStream(receiver));
    }


    public void start(){
        try{
            stream.window(new Duration(2000),new Duration(6000)).foreachRDD(new PersistForEachRDD());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        context.start();
    }

    public void stop(){
        context.stop();
    }

    public void close(){
        context.close();
    }

    public void startAndAwait(){
        start();
        context.awaitTermination();
    }


}
