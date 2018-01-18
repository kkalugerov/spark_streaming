package analytics;

import model.Model;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.doccat.*;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.*;
import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.StringUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class CoreNLP {
    private static final Logger logger = LoggerFactory.getLogger(CoreNLP.class);
    private static CoreNLP INSTANCE;
    private static TokenizerME tokenizer;
    private static NameFinderME personFinder;
    private static NameFinderME locationFinder;
    private static NameFinderME organizationFinder;
    private static DocumentCategorizerME categorizer;
    private static ChunkerME chunker;
    private static POSTagger posTagger;
    private static TokenizerModel tokenizerModel;
    private static TokenNameFinderModel personModel;
    private static TokenNameFinderModel locationModel;
    private static TokenNameFinderModel organizationModel;
    private static ChunkerModel chunkerModel;
    private static POSModel posModel;
    private static DoccatModel doccatModel;
    private static Set<String> stopWords;
    private static InputStream inputStreamStopWords;


    public static CoreNLP getInstance() throws Exception {
        initModels();
        trainModel();
        stopWords =  new HashSet<>(IOUtils.readLines(inputStreamStopWords, "UTF-8"));
        tokenizer = new TokenizerME(tokenizerModel);
        personFinder = new NameFinderME(personModel);
        locationFinder = new NameFinderME(locationModel);
        organizationFinder = new NameFinderME(organizationModel);
        chunker = new ChunkerME(chunkerModel);
        posTagger = new POSTaggerME(posModel);
        categorizer = new DocumentCategorizerME(doccatModel);
        if (INSTANCE == null) {
            synchronized (CoreNLP.class) {
                INSTANCE = new CoreNLP();
            }
        }

        return INSTANCE;
    }

    private static void initModels() throws IOException {
        inputStreamStopWords = new FileInputStream(new File("")
                .getAbsolutePath() + "/src/main/resources/stopwords.txt");
        tokenizerModel = new TokenizerModel(new FileInputStream(new File("")
                .getAbsolutePath() + "/src/main/resources/models/en-token.bin"));
        personModel = new TokenNameFinderModel(new FileInputStream(new File("")
                .getAbsolutePath() + "/src/main/resources/models/en-ner-person.bin"));
        locationModel = new TokenNameFinderModel(new FileInputStream(new File("")
                .getAbsolutePath() + "/src/main/resources/models/en-ner-location.bin"));
        organizationModel = new TokenNameFinderModel(new FileInputStream(new File("")
                .getAbsolutePath() + "/src/main/resources/models/en-ner-organization.bin"));
        chunkerModel = new ChunkerModel(new FileInputStream(new File("")
                .getAbsolutePath() + "/src/main/resources/models/en-chunker.bin"));
        posModel = new POSModel(new FileInputStream(new File("")
                .getAbsolutePath() + "/src/main/resources/models/en-pos-maxent.bin"));

    }

    public static void trainModel() {
        try {
            InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(
                    new File("/home/zealot/IdeaProjects/" +
                            "spark_twitter_streaming/src/main/resources/training/training_tweets.txt"));
            ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, "UTF-8");
            ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);

            doccatModel = DocumentCategorizerME
                    .train("en", sampleStream, TrainingParameters.defaultParams(), new DoccatFactory());
        } catch (IOException e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    public String classify(String content) {
        String sentiment;
        String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(content);
        double[] outcomes = categorizer.categorize(tokens);
        String category = categorizer.getBestCategory(outcomes);
        if (category.equalsIgnoreCase("1"))
            sentiment = "POSITIVE";
        else if (category.equalsIgnoreCase("0"))
            sentiment = "NEGATIVE";
        else
            sentiment = "NEUTRAL";

        return sentiment;
    }

    public static String[] tokenize(String sentence) {
        try {
            return tokenizer.tokenize(sentence);
        } catch (Exception ex) {
            ex.getMessage();
            ex.getCause();
        }
        return new String[0];
    }

    public Set<String> findNEPerson(String content) {
        Set<String> persons = new HashSet<>();
        String[] tokens = tokenize(content);
        if (tokens.length > 0) {
            Span[] nameSpans = personFinder.find(tokens);
            String[] namedEntities = (Span.spansToStrings(nameSpans, tokens));
            persons.addAll(Arrays.asList(namedEntities));
            return persons;
        }

        if (!persons.isEmpty())
            logger.info("Person named entities found " + persons);
        else
            logger.info("No named entities person found !");

        return new HashSet<>();
    }

    public Set<String> findNELocation(String content) {
        Set<String> locations = new HashSet<>();
        String[] tokens = tokenize(content);
        if (tokens.length > 0) {
            Span[] nameSpans = locationFinder.find(tokens);
            String[] namedEntities = (Span.spansToStrings(nameSpans, tokens));
            locations.addAll(Arrays.asList(namedEntities));
            return locations;
        }

        if (!locations.isEmpty())
            logger.info("Named entities of type location found  " + locations);
        else
            logger.info("No named entities of type location found !");

        return new HashSet<>();
    }

    public Set<String> findNEOrganization(String content) {
        Set<String> organizations = new HashSet<>();
        String[] tokens = tokenize(content);
        if (tokens.length > 0) {
            Span[] nameSpans = organizationFinder.find(tokens);
            String[] namedEntities = (Span.spansToStrings(nameSpans, tokens));
            organizations.addAll(Arrays.asList(namedEntities));
            return organizations;
        }

        if (!organizations.isEmpty())
            logger.info("Named entities of type location found  " + organizations);
        else
            logger.info("No named entities of type location found !");

        return new HashSet<>();
    }

    public void chunker(String content) {
        String[] tokens = tokenize(content);
        String[] posTags = posTagger.tag(tokens);
        String[] chunks = chunker.chunk(tokens, posTags);
        for (String chunk : chunks)
            System.out.println(chunk);

    }

    private Map<String, Set<String>> extractCashTagHashTagAndMentions(List<String> tokens) {
        Map<String, Set<String>> extractedCashTagHashTagsAndMentions = new HashMap<>();

        Set<String> hashtags = tokens.stream()
                .distinct()
                .filter(token -> token.startsWith("#"))
                .map(token -> token.replaceAll("[^a-zA-Z0-9#]", "").trim().toLowerCase())
                .filter(hashtag -> !hashtag.equalsIgnoreCase("#"))
                .collect(Collectors.toSet());

        Set<String> cashtags = tokens.stream()
                .distinct()
                .filter(token -> token.startsWith("$") && !StringUtils.isNumeric(token.substring(1)) && !token.contains("."))
                .collect(Collectors.toSet());

        Set<String> mentions = tokens.stream()
                .distinct()
                .filter(s -> s.startsWith("@"))
                .collect(Collectors.toSet());
        mentions.forEach(s -> {
            if (s.contains(":")) s = s.substring(0, s.indexOf(":"));
        });

        extractedCashTagHashTagsAndMentions.put("@", mentions);
        extractedCashTagHashTagsAndMentions.put("#", hashtags);
        extractedCashTagHashTagsAndMentions.put("$", cashtags);

        return extractedCashTagHashTagsAndMentions;
    }

    public void processWithAnalytics(Model model) {
        String content = model.getContent();
        content = StringUtils.removeURLs(content).replaceAll("[^\\w\\s]", "");
        String clearContent= "";
        for (String word : stopWords)
            clearContent = content.replaceAll("\\s+" + word + "\\s+", " ");

        Map<String, Set<String>> cashtagsAndhastags =
                extractCashTagHashTagAndMentions(Arrays.asList(tokenize(clearContent)));
        Object mentions = cashtagsAndhastags.values().toArray()[0];
        Object hashtags = cashtagsAndhastags.values().toArray()[1];
        Object cashtags = cashtagsAndhastags.values().toArray()[2];
        model.setSentiment(classify(clearContent));
        model.setPersons(findNEPerson(clearContent));
        model.setOrganizations(findNEOrganization(clearContent));
        model.setLocations(findNELocation(clearContent));
        model.setHashtags((Set<String>) hashtags);
        model.setCashtags((Set<String>) cashtags);
        model.setMentions((Set<String>) mentions);

    }

    public static void main(String[] args) {
    }

}
