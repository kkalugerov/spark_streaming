package receivers;

import model.Document;
import model.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.util.Strings;
import org.apache.spark.storage.StorageLevel;
import twitter4j.*;
import twitter4j.auth.Authorization;

import java.util.*;

public class TwitterReceiver<T> extends MainReceiver<T> {

    private final Properties properties;
    private final Authorization auth;
    private final long[] follow;
    private String[] track = new String[0];
    private Integer RESTART_AFTER = 10 * 60 * 1000;// 10 min
    private int minFollowers;


    private volatile TwitterStream twitterStream;
    private String identity = "Unknown";

    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(TwitterReceiver.class);

    public TwitterReceiver(String identity, Properties prop, StorageLevel storageLevel, Authorization auth,
                           long[] follow, String[] track) {

        super(storageLevel);

        this.auth = auth;
        this.follow = follow;

        List<String> trackList = new ArrayList<>();
        Arrays.asList(track).forEach(s -> trackList.add(s.trim()));
        this.track = new String[trackList.size()];
        trackList.toArray(this.track);

        this.properties = prop;
        this.identity = identity;

        if (prop.getProperty("restart.after") != null && !prop.getProperty("restart.after").isEmpty()) {
            try {
                RESTART_AFTER = Integer.parseInt(prop.getProperty("restart.after"));
                logger.info("Twitter config loaded: RESTART_AFTER=" + RESTART_AFTER);
            } catch (NumberFormatException e) {
                logger.info("Num Format Ex; used default RESTART_AFTER=" + RESTART_AFTER);
            }
        }

        if (prop.getProperty("twitter.min.followers") != null && !prop.getProperty("twitter.min.followers").isEmpty()) {
            try {
                minFollowers = Integer.parseInt(prop.getProperty("twitter.min.followers"));
            } catch (NumberFormatException e) {
                logger.error("Cannot parse to Integer twitter.min.followers: " + prop.getProperty("twitter.min.followers"));
            }
        }
        logger.info("minFollowers: " + minFollowers);
    }

    public TwitterReceiver(String identity, Properties prop, Authorization auth, long[] follow,
                           String[] track) {
        this(identity, prop, StorageLevel.MEMORY_AND_DISK_2(), auth, follow, track);
    }


    /**
     * We override this  method because we do not want to create new Thread because Twitter4j creates it internally
     */
    @Override
    public void onStart() {
        receive();
    }

    /**
     * Register twitter listener (filtered or sample) and store each twit to the stream for further processing
     */
//    @Override
    protected void receive() {
        try {

            logger.info("Initializing Twitter Receiver...");
            TwitterStream newTwitterStream = new TwitterStreamFactory().getInstance(auth);

            FilterQuery filter = new FilterQuery();
            boolean useFilter = false;
            if (follow != null && follow.length > 0) {
                filter.follow(follow);
                useFilter = true;
            }
            if (track != null && Strings.isNotEmpty(track[0])) {
                filter.track(track);
                useFilter = true;
            }

            newTwitterStream.addListener(new RawStreamListener() {
                private Document toDocument(Status status, String rawJson) {
                    Model model = new Model();

                    model.setContent(status.getText());
                    model.setLang(status.getLang());
                    model.setRawJson(rawJson);

                    if (model.getLang().equalsIgnoreCase("en"))
                        return new Document(model, true);
                    return new Document(model, false);
                }

                @Override
                public void onException(Exception ex) {
                    logger.info("onException: " + ex);
                    if (!isStopped())
                        restart("Error onException - Twitter receiver", ex, RESTART_AFTER);
                }

                @Override
                public void onMessage(String rawString) {

                    try {
                        Status status = null;
                        try {
                            status = TwitterObjectFactory.createStatus(rawString);

                        } catch (TwitterException twe) {
                            logger.error("TwitterException: " + twe.getCause().getMessage());
                            logger.error("TwitterException: " + twe.getErrorMessage());

                            if (!isStopped()) {
                                restart("Restarting  Twitter receiver ...", twe, RESTART_AFTER);
                            }
                        }

                        assert status != null;
                        Document document = toDocument(status, rawString);
                        if (document.getProcess())
                            store((T) document);

                    } catch (Exception e) {
                        logger.info(e.getStackTrace());
                        logger.info(e.getMessage());
                        logger.info(e.getCause());
                        if (!isStopped())
                            restart("Restarting  Twitter receiver ...", e, RESTART_AFTER);
                    }
                }
            });

            if (useFilter) newTwitterStream.filter(filter);

            else newTwitterStream.sample();

            if (follow == null) logger.info("follow: null");

            else Arrays.asList(follow).forEach(logger::info);

            logger.info("Track: ");

            if (track == null) logger.info("track: null");

            else Arrays.asList(track).forEach(logger::info);

            setTwitterStream(newTwitterStream);
        } catch (Exception ex) {
            if (!isStopped())
                restart("Error starting Twitter stream", ex, RESTART_AFTER);
        }
    }

    @Override
    public void onStop() {
        setTwitterStream(null);
    }

    private synchronized void setTwitterStream(TwitterStream newTwitterStream) {
        if (twitterStream != null) {
            twitterStream.shutdown();
        }
        twitterStream = newTwitterStream;
    }

}
