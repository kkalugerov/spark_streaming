package functions;

import elasticsearch.ElasticConnector;
import model.Document;
import org.apache.spark.api.java.function.VoidFunction;
import processing.Processing;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class PersistPartitionFunction implements VoidFunction<Iterator<Document>> {
    private ElasticConnector elasticConnector = ElasticConnector.getInstance();

    @Override
    public void call(Iterator<Document> documentIterator) {
        List<Document> documents = new ArrayList<>();
        try {
            while (documentIterator.hasNext())
                documents.add(Processing.getInstance().process(documentIterator.next()));
            elasticConnector.toElastic(documents);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private synchronized void persist(List<Document> documents) {

    }

}
