package utils;

import model.Document;
import org.apache.logging.log4j.LogManager;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.util.List;

public class JsonUtils {

    private static JSONObject index;
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(JsonUtils.class);

    public static JSONObject toIndex(List<Document> documents) {

        if (documents.isEmpty())
            return new JSONObject();

        documents.forEach(document ->
        {
            index = new JSONObject();
            if (document.getModel().getLang().equalsIgnoreCase("en"))
                try {
                    index.put("Content", document.getModel().getContent());
                    index.put("RawJson", document.getModel().getRawJson());
                    index.put("Keywords", document.getModel().getKeywords());
                    index.put("Sentiment", document.getModel().getSentiment());
                    index.put("Hashtags", document.getModel().getHashtags());
                    index.put("Mentions", document.getModel().getMentions());
                    index.put("Cashtags", document.getModel().getHashtags());
                    index.put("Locations", document.getModel().getLocations());
                    index.put("Organizations", document.getModel().getOrganizations());
                    index.put("Persons", document.getModel().getPersons());
                    index.put("Lang", document.getModel().getLang());
                    index.put("timestamp", new java.util.Date());
                    logger.info("Document to be stored - > " + index.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        });
        return index;
    }

}
