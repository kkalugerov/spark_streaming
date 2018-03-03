package utils;

import model.Document;
import model.Model;
import org.apache.logging.log4j.LogManager;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.util.List;

public class JsonUtils {

    private static JSONObject index;
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(JsonUtils.class);

    public static JSONObject toIndex(List<Model> models) {

        if (models.isEmpty())
            return new JSONObject();

        models.forEach(model ->
        {
            index = new JSONObject();
                try {
                    index.put("Content", model.getContent());
                    index.put("RawJson", model.getRawJson());
                    index.put("Keywords", model.getKeywords());
                    index.put("Sentiment", model.getSentiment());
                    index.put("Hashtags", model.getHashtags());
                    index.put("Mentions", model.getMentions());
                    index.put("Cashtags", model.getHashtags());
                    index.put("Locations", model.getLocations());
                    index.put("Organizations", model.getOrganizations());
                    index.put("Persons", model.getPersons());
                    index.put("Lang", model.getLang());
                    index.put("timestamp", new java.util.Date());
                    logger.info("Document to be stored - > " + index.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        });
        return index;
    }

}
