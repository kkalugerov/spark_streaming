package functions;

import database.MongoConnector;
import elasticsearch.ElasticConnector;
import model.Document;
import model.Model;
import org.apache.spark.api.java.function.VoidFunction;
import processing.Processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PersistPartitionFunction implements VoidFunction<Iterator<Model>> {

    public ElasticConnector elasticConnector = new ElasticConnector();
    public MongoConnector mongoConnector = new MongoConnector();

    @Override
    public void call(Iterator<Model> modelIterator) {
        List<Model> models = new ArrayList<>();
        try {
            while (modelIterator.hasNext())
                models.add(Processing.getInstance().process(modelIterator.next()));
            mongoConnector.insertToCollection("twitter",models);
//            elasticConnector.toElastic(models);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private synchronized void persist(List<Document> documents) {

    }

}
