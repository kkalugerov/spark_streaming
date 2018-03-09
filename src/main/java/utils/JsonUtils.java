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
        if (models.isEmpty()) {
            return new JSONObject();
        }

        models.forEach(model ->
        {
            try {
                index = new JSONObject();
                index.put("content", model.getContent());
                index.put("keywords", model.getKeywords());
                index.put("sentiment", model.getSentiment());
                index.put("hashtags", model.getHashtags());
                index.put("mentions", model.getMentions());
                index.put("cashtags", model.getHashtags());
                index.put("locations", model.getLocations());
                index.put("organizations", model.getOrganizations());
                index.put("persons", model.getPersons());
                index.put("lang", model.getLang());
                index.put("timestamp", new java.util.Date());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        logger.info("Document to be stored - > " + index.toString());
        return index;
    }

}
