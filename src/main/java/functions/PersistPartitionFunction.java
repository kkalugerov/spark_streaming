package functions;

import model.Document;
import org.apache.spark.api.java.function.VoidFunction;
import processing.Processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PersistPartitionFunction implements VoidFunction<Iterator<Document>> {

    @Override
    public void call(Iterator<Document> documentIterator) {
        List<Document> documents = new ArrayList<>();
        try{
            while(documentIterator.hasNext())
                documents.add(Processing.getInstance().process(documentIterator.next()));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private synchronized  void persist(List<Document> documents){};
}
