package receivers;

import model.Document;
import model.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.util.Strings;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;
import twitter4j.*;
import twitter4j.auth.Authorization;

import java.util.*;

public class TwitterReceiver<T> extends MainReceiver<T> {

    private double SPAM_THRESHOLD; // Threshold for a tweet's individual spam score
    private final String ORIGIN = "twitter";
    private final String SOURCE = "twitter";
    private final Properties properties;
    private final Authorization auth;
    private final long[] follow;
    private String[] track = new String[0];
    private Integer RESTART_AFTER = 10 * 60 * 1000;// 10 min
    private int minKeywords;
    private int minFollowers;
    private String[] keywordsLowerCase = {"refugee", "refugees", "Terrorism", "Al Qaeda", "Terror", "Attack", "Iraq",
            "Afghanistan", "Iran", "Pakistan", "Agro", "Environmental terrorist", "Eco terrorism", "Conventional weapon",
            "Weapons grade", "Dirty bomb", "Enriched", "Nuclear", "Chemical weapon", "Biological weapon",
            "Ammonium nitrate", "Improvised explosive device", "IED ", "Abu Sayyaf", "Hamas", "FARC ", "IRA ", "ETA ",
            "Basque Separatists", "Hezbollah", "Tamil Tigers", "PLF ", "PLO ", "Car bomb", "Jihad", "Taliban",
            "Weapons cache", "Suicide bomber", "Suicide attack", "Suspicious substance", "AQAP", "AQIM", "TTP", "Yemen",
            "Extremism", "Somalia", "Nigeria", "Radicals", "Al-Shabaab", "Home grown", "Nationalist", "Recruitment",
            "Fundamentalism", "Islamist", "Tamils Sri Lanka", "Assassination", "Domestic security", "Law enforcement",
            "Authorities", "Disaster assistance", "Disaster management", "DNDO", "National preparedness", "Mitigation",
            "Prevention", "Response", "Recovery", "Dirty bomb", "Domestic nuclear detection", "Emergency management",
            "Emergency response", "First responder", "Homeland security", "Maritime domain awareness", "Militia",
            "Shooting", "Shots fired", "Evacuation", "Deaths", "Hostage", "Explosion ", "Police", "DMAT",
            "Organized crime", "Gangs", "National security", "State of emergency", "Security", "Breach", "Threat",
            "Standoff", "SWAT", "Screening", "Lockdown", "Bomb", "Crash", "Looting", "Riot", "Emergency Landing",
            "Pipe bomb", "Incident"};

    // List of most commonly used words in spam and scam messages and emails
    private String[] spamWordsAndPhrases;
    private Set<String> spamExcludeWordsAndPhrases = new HashSet<>();

    private volatile TwitterStream twitterStream;
    private String project;
    private List<String> languages;
    private String identity = "Unknown";

    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(TwitterReceiver.class);

    public TwitterReceiver(String identity, Properties prop, StorageLevel storageLevel, Authorization auth,
                           long[] follow, String[] track, String project) {

        super(storageLevel);

        this.auth = auth;
        this.follow = follow;

        List<String> trackList = new ArrayList<>();
        Arrays.asList(track).forEach(s -> trackList.add(s.trim()));
        this.track = new String[trackList.size()];
        trackList.toArray(this.track);

        this.properties = prop;
        this.identity = identity;
        this.project = project;


        if (prop.getProperty("restart.after") != null && !prop.getProperty("restart.after").isEmpty()) {
            try {
                RESTART_AFTER = Integer.parseInt(prop.getProperty("restart.after"));
                logger.info("Twitter config loaded: RESTART_AFTER=" + RESTART_AFTER);
            } catch (NumberFormatException e) {
                logger.info("Num Format Ex; used default RESTART_AFTER=" + RESTART_AFTER);
            }
        }

        if (prop.getProperty("twitter.spam.threshold") != null && !prop.getProperty("twitter.spam.threshold").isEmpty()) {
            try {
                SPAM_THRESHOLD = Double.parseDouble(prop.getProperty("twitter.spam.threshold"));
            } catch (NumberFormatException e) {
                logger.error("Cannot parse to Double twitter.spam.threshold: " + prop.getProperty("twitter.spam.threshold"));
            }
        }
        logger.info("SPAM_THRESHOLD: " + SPAM_THRESHOLD);

        if (prop.getProperty("twitter.min.followers") != null && !prop.getProperty("twitter.min.followers").isEmpty()) {
            try {
                minFollowers = Integer.parseInt(prop.getProperty("twitter.min.followers"));
            } catch (NumberFormatException e) {
                logger.error("Cannot parse to Integer twitter.min.followers: " + prop.getProperty("twitter.min.followers"));
            }
        }
        logger.info("minFollowers: " + minFollowers);

        if (prop.getProperty("twitter.languages") != null && !prop.getProperty("twitter.languages").isEmpty()) {
            languages = Arrays.asList(prop.getProperty("twitter.languages").toLowerCase().split(","));
        }

        if (prop.getProperty("spam.words") != null && !prop.getProperty("spam.words").isEmpty()) {
            spamWordsAndPhrases = prop.getProperty("spam.words").toLowerCase().split(",");
        }
        logger.info("SPAM_WORDS: " + Arrays.asList(spamWordsAndPhrases));

        if (prop.getProperty("spam.exclude.words") != null && !prop.getProperty("spam.exclude.words").isEmpty()) {
            spamExcludeWordsAndPhrases.addAll(Arrays.asList(prop.getProperty("spam.exclude.words").toLowerCase().split(",")));
        } else {
            spamExcludeWordsAndPhrases.addAll(Arrays.asList("ahole", "arsch", "asshole", "bagina", "basterd",
                    "be otch", "beatmymeat", "beatoff", "biatch", "bitchin'", "bitching", "bullshit", "bung",
                    "bunghole", "butcrack", "butt sex", "butterflybutt", "butthole", "buttsex", "butt-sex",
                    "caca", "ceemen", "cherry popper", "chienne", "chier", "circlejerk", "clit", "cmorebutt",
                    "cohones", "cojones", "come box", "comeonrideit", "cum", "cunni", "cunnilingus", "cunny", "cunt",
                    "cyberme", "damn", "damned", "dickhead", "dildo", "dipshit", "doggystyle", "duelarsing", "eat me",
                    "efilnkufecin", "enculeur", "enima", "f*ck", "fart", "felletio", "f'in", "f'ing", "fingerme", "fkg",
                    "freakin'", "freaking", "fuck", "fucked", "fucker", "fuckin'", "fucking", "gangbang", "gethead",
                    "getting laid", "giuemeabj", "give head", "give me head", "giveabj", "givebj", "gizz", "godamn",
                    "goddam", "goddammit", "goddamn", "goodhead", "hardon", "hctib", "herpees", "herpes", "highon",
                    "himen", "horney", "horny", "hotchat", "hugehooters", "hugetit", "humper", "hyman", "i pee freely",
                    "ieatm", "ifintermyself", "ilvtofkwthu", "iplaywithmyself", "ivegotabigone", "ivegotahugeone",
                    "jack me", "jack off", "jackme", "jackoff", "jerk off", "jerkme", "jerkoff", "jiz", "jizm",
                    "jodegas", "kcuf", "kefe", "kinki", "kike", "kock", "kunt", "kyke", "letscyber", "likmydic",
                    "master ba", "masterbate", "mastrbater", "merde", "mikehock", "mikerotch", "motherfucker",
                    "muff", "mussilini", "nakid", "pee", "pelotas", "phuck", "phyllus", "piss", "pistoff", "poontang",
                    "poop", "poopoo", "porn", "pouss", "pusee", "ritard", "schit", "schlong", "schmuk", "shit",
                    "shitting", "shitty", "shlong", "shmuck", "shmuk", "sifilis", "sissy", "sitonmyface", "skrotum",
                    "slut", "spankurbutt", "ux", "tard", "teetz", "tetons", "titie", "turd", "twaat", "twat",
                    "vibrater", "wuss", "wussy", "anal", "creampie", "orgasm", "porn", "blowjob", "bukkake",
                    "creampie", "cuckold", "cumshots", "gangbang", "handjob", "orgasm", "orgy", "porno",
                    "threesome", "cum", "cumshot", "dildo", "fingering", "fisting", "gangbang", "gape", "squirt",
                    "hentai", "asshole", "clit", "nipples", "pussy lips", "cunt", "cock docking", "cock stuffing",
                    "cock sucking", "deepthroat", "doggystyle", "facial cumshot", "facefuck", "gloryhole",
                    "jerking off", "facesitting", "wank", "slut", "vibrator", "squirting", "strapon porn"));
        }
        logger.info("SPAM_WORDS_EXCLUDE: " + spamExcludeWordsAndPhrases);

        if (prop.getProperty("drain.content.minKeywords") != null && prop.getProperty("drain.content.keywords")
                != null && !prop.getProperty("drain.content.keywords").isEmpty()) {
            keywordsLowerCase = prop.getProperty("drain.content.keywords").toLowerCase().split(",");
            try {
                minKeywords = Integer.parseInt(prop.getProperty("drain.content.minKeywords"));
            } catch (NumberFormatException e) {
                logger.info("Num Format Ex; used default minKeywords=" + minKeywords);
            }
        }
    }

    public TwitterReceiver(String identity, Properties prop, Authorization auth, long[] follow,
                           String[] track, String project) {
        this(identity, prop, StorageLevel.MEMORY_AND_DISK_2(), auth, follow, track, project);
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
                    if(model.getLang().equalsIgnoreCase("en"))
                        return new Document(model, true);
                    return new Document(model,false);
                }

                @Override
                public void onException(Exception ex) {
                    logger.info("onException: " + ex);
                    if (!isStopped())
                        restart("Error onException - Twitter receiver", ex, RESTART_AFTER);
                }

                @Override
                public void onMessage(String rawString) {

                    if (rawString == null || rawString.isEmpty() || rawString.trim().isEmpty()) return;

                    try {
                        Status status = null;
                        try {
                            status = TwitterObjectFactory.createStatus(rawString);
                            logger.info("CREATED_AT: " + status.getCreatedAt());

                        } catch (TwitterException twe) {
                            logger.error("TwitterException: " + twe);
                            logger.error("TwitterException: " + twe.getCause());
                            logger.error("TwitterException: " + twe.getCause().getMessage());
                            logger.error("TwitterException: " + twe.getErrorMessage());

                            if (!isStopped()) {
                                restart("Restarting  Twitter receiver ...", twe, RESTART_AFTER);
                            }
                        }

                        if (status.getText().isEmpty() || status.getText() == null) return;
                        else {
                            Document document = toDocument(status, rawString);
                            store((T) document);
                        }
                    } catch (Exception e) {
                        logger.info(e.getStackTrace());
                        logger.info(e.getMessage());
                        logger.info(e.getCause());
                        logger.info(e.getCause().getMessage());
                        if (!isStopped())
                            restart("Restarting  Twitter receiver ...", e, RESTART_AFTER);
                    }
                }
            });

            if (useFilter) newTwitterStream.filter(filter);

            else newTwitterStream.sample();

            logger.info("Twitter receiver info: \nuse filter = " + useFilter + " \nFollows: \n");

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

    private boolean filterLang(String lang) {
        if (languages.contains(lang)) return true;

        if (lang.contains("-")) {
            String[] langVariants = lang.split("-");
            return languages.contains(langVariants[0]);
        }

        return false;
    }

    private boolean processTweet(String content) {
        if (content.length() < 10 || containsPorn(content))
            return false;
        else return (getSpamScore(content) > SPAM_THRESHOLD);
    }

    private int spamWordsCheck(String tweet) {

        int spamWordsCount = 0;
        for (String spamWord : spamWordsAndPhrases) {
            if (tweet.toLowerCase().contains(spamWord)) {
                spamWordsCount++;
            }
        }
        return spamWordsCount;
    }

    private boolean containsPorn(String content) {
        List<String> contentSplit = Arrays.asList(content.split(" "));
        for (String token : contentSplit) {
            if (spamExcludeWordsAndPhrases.contains(token.toLowerCase().trim())) {
                logger.info("REJECTED_CONTENT: " + token);
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the spal score for the tweet.
     */
    private double getSpamScore(String content) {
        int userMentions = 0;
        int hashtagsCount = 0;
        int webLinksCount = 0;
        int spamWordsCount = spamWordsCheck(content); // Extra iteration over the spam words in Utils list
        int nonAlphaCount = 0;
        List<String> contentSplit = Arrays.asList(content.split(" "));
        for (String token : contentSplit) {
            if (token.startsWith("@") && token.length() > 2) {
                userMentions++;
                continue;
            }
            if (token.startsWith("#")) {
                hashtagsCount++;
                continue;
            }
            if (token.startsWith("http")) {
                webLinksCount++;
                continue;
            }
            if (!token.matches("[a-zA-Z0-9'-,.?!:;]+")) {
                nonAlphaCount++;
            }
        }
        double finalScore = ((double) (userMentions + hashtagsCount + webLinksCount + spamWordsCount + nonAlphaCount)) / contentSplit.size();
        finalScore = 1.0d - finalScore;

        logger.info("SPAM_SCORE: " + String.format("%.2f", finalScore) + " " + content);
        return finalScore;
    }
}
