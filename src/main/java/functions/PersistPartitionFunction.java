package functions;

import model.Document;
import model.Model;
import org.apache.spark.api.java.function.VoidFunction;
import processing.Processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PersistPartitionFunction implements VoidFunction<Iterator<Document>> {

    @Override
    public void call(Iterator<Document> documentIterator) {
        List<Document> document = new ArrayList<>();
        try{
            while(documentIterator.hasNext())
                document.add(Processing.getInstance().process(documentIterator.next()));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private synchronized  void persist(List<Model> models){};
}
