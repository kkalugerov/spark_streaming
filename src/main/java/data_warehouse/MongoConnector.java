package data_warehouse;

import analytics.CoreNLP;
import com.mongodb.MongoClient;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import model.Model;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;


import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MongoConnector implements Serializable {
    private static Properties properties = new Properties();
    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;
    private static String databaseName;
    private static Document mongoDoc;

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(MongoConnector.class);

    public MongoConnector() {
        loadProps();
        init();
        makeConnectionToDatabase();

    }

    private static void loadProps() {
        InputStream inputStream = CoreNLP.class.getClassLoader().getResourceAsStream("mongodb.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void init() {
        String host = properties.getProperty("mongo.host");
        int port = Integer.parseInt(properties.getProperty("mongo.port"));
        databaseName = properties.getProperty("mongo.database");
        mongoClient = new MongoClient(new ServerAddress(host, port));
    }

    private MongoDatabase makeConnectionToDatabase() {
        try {
            MongoIterable<String> databases = mongoClient.listDatabaseNames();
            if (databases.into(new ArrayList<>()).contains(databaseName)) {
                logger.info("Connection to database -> " + databaseName + " is successful !");
                mongoDatabase = mongoClient.getDatabase(databaseName);
                return mongoDatabase;
            } else {
                logger.error("No such database ");
                mongoDatabase = mongoClient.getDatabase(databaseName);
                logger.info("New database has been created !");
            }
        } catch (MongoSocketOpenException ex) {
            logger.error(ex.getMessage());
        }
        return mongoDatabase;
    }

    private MongoCollection<Document> getCollection(String collectionName) {
//        MongoDatabase existingDB = makeConnectionToDatabase();
        MongoIterable<String> collections = mongoDatabase.listCollectionNames();

        if (collections.into(new ArrayList<>()).contains(collectionName)) {
            logger.info("Accessing the collection -> " + collectionName + " is successful ! ");
            return mongoDatabase.getCollection(collectionName);
        } else {
            logger.error("No such collection !");
            MongoCollection<Document> newCollection = mongoDatabase.getCollection(collectionName);
            logger.info("New collection with name -> " + collectionName + "has been created !");
            return newCollection;
        }
    }

    public void insertToCollection(String collectionName, List<Model> models) {

        MongoCollection<Document> existingCollection = getCollection(collectionName);
        existingCollection.insertOne(toMongoDoc(models));
        logger.info("Document inserted successful !");
    }

    private static Document toMongoDoc(List<Model> models) {
        if (models.isEmpty()) {
            return new Document();
        }

        models.forEach(model ->
        {
            mongoDoc = new Document();
            mongoDoc.put("content", model.getContent());
            mongoDoc.put("keywords", model.getKeywords());
            mongoDoc.put("sentiment", model.getSentiment());
            mongoDoc.put("hashtags", model.getHashtags());
            mongoDoc.put("mentions", model.getMentions());
            mongoDoc.put("locations", model.getLocations());
            mongoDoc.put("organizations", model.getOrganizations());
            mongoDoc.put("persons", model.getPersons());
            mongoDoc.put("lang", model.getLang());
            mongoDoc.put("timestamp", new java.util.Date());
        });
        logger.info("Document to be stored - > " + mongoDoc.toString());
        return mongoDoc;
    }

    //    public void insertToCollection(String collectionName, List<Model> models) {
//        MongoIterable<String> databases = mongoClient.listDatabaseNames();
//        MongoCollection<Document> collection;
//        if (databases.into(new ArrayList<>()).contains(databaseName))
//            mongoDatabase = mongoClient.getDatabase(databaseName);
//        else
//            mongoDatabase = mongoClient.getDatabase(databaseName);
//
//        MongoIterable<String> collections = mongoDatabase.listCollectionNames();
//        if (collections.into(new ArrayList<>()).contains(collectionName)) {
//            collection = mongoDatabase.getCollection(collectionName);
//            collection.insertOne(toMongoDoc(models));
//            logger.info("Document inserted successful !");
//        } else {
//            collection = mongoDatabase.getCollection(collectionName);
//            collection.insertOne(toMongoDoc(models));
//            logger.info("Document inserted successful !");
//        }
//    }
}
