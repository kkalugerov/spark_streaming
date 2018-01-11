package receivers;

import model.Document;
import twitter4j.auth.Authorization;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.ConfigurationBuilder;
import utils.StringUtils;

import java.util.*;

public class ReceiverFactory {

    private static Collection<MainReceiver<Document>> createReceivers(String receiverName, Properties properties) {
        return addTwitterReceivers(properties);
    }

    public static Collection<MainReceiver<Document>> getReceivers(String[] streams, Properties properties) {

        Collection<MainReceiver<Document>> receivers = new ArrayList<>();

        for (String stream : streams) {
            Collection<MainReceiver<Document>> mainReceivers = ReceiverFactory.createReceivers(stream, properties);
            if (mainReceivers != null)
                receivers.addAll(mainReceivers);
        }

        return receivers;
    }

    private static Collection<MainReceiver<Document>> addTwitterReceivers(Properties properties) {
        Collection<MainReceiver<Document>> receivers = new ArrayList<>();


        List<String> consumerKeys = Arrays.asList(properties.getProperty("twitter.oauth.consumerKey")
                .split(StringUtils.MULTI_ARGUMENTS_DELIMITER));
        List<String> consumerSecrets = Arrays.asList(properties.getProperty("twitter.oauth.consumerSecret")
                .split(StringUtils.MULTI_ARGUMENTS_DELIMITER));
        List<String> accessTokens = Arrays.asList(properties.getProperty("twitter.oauth.accessToken")
                .split(StringUtils.MULTI_ARGUMENTS_DELIMITER));
        List<String> accessTokenSecrets = Arrays.asList(properties.getProperty("twitter.oauth.accessTokenSecret")
                .split(StringUtils.MULTI_ARGUMENTS_DELIMITER));

        List<List<String>> followList = StringUtils.gatherProps(properties.getProperty("twitter.follow"));
        List<List<String>> trackList = StringUtils.gatherProps(properties.getProperty("twitter.track"));

        List<String> projects = Arrays.asList(properties.getProperty("twitter.project")
                .split(StringUtils.MULTI_ARGUMENTS_DELIMITER));

        if (!(consumerSecrets.size() >= consumerKeys.size() &&
                accessTokens.size() >= consumerKeys.size() &&
                accessTokenSecrets.size() >= consumerKeys.size() &&
                projects.size() >= consumerKeys.size())) {
            throw new IllegalArgumentException("Invalid twitter credentials");
        }

        for (int i = 0; i < consumerKeys.size(); i++) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKeys.get(i))
                    .setOAuthConsumerSecret(consumerSecrets.get(i))
                    .setOAuthAccessToken(accessTokens.get(i))
                    .setOAuthAccessTokenSecret(accessTokenSecrets.get(i));

            //http.connectionTimeout	Http connection timeout in milliseconds	Default:20000
            cb.setHttpConnectionTimeout(60000);
            cb.setPrettyDebugEnabled(true);
            cb.setHttpRetryCount(3);

            Authorization auth = new OAuthAuthorization(cb.build());

            List<String> trackStr = trackList.get(i);

            long[] follow = null;
            if (followList != null && !followList.isEmpty()) {
                List<String> followStr = followList.get(i);
                if (followStr == null) {
                    followStr = new ArrayList<>();
                }

                follow = new long[followStr.size()];
                for (int j = 0; j < followStr.size(); j++) {
                    try {
                        follow[j] = 0L;
                        follow[j] = Long.valueOf(followStr.get(j).trim());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
            receivers.add(new TwitterReceiver("TwitterReceiver#" + i, properties, auth, follow,
                    trackStr.toArray(new String[trackStr.size()]), projects.get(i)));
        }

        return receivers;
    }

}
