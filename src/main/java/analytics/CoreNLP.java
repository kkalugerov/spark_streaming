package analytics;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import model.Model;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.doccat.*;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.*;
import org.apache.commons.io.IOUtils;
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
    private static NameFinderME personFinder;
    private static NameFinderME locationFinder;
    private static NameFinderME organizationFinder;
    private static DocumentCategorizerME categorizer;
    private static ChunkerME chunker;
    private static POSTagger posTagger;
    private static TokenNameFinderModel personModel;
    private static TokenNameFinderModel locationModel;
    private static TokenNameFinderModel organizationModel;
    private static ChunkerModel chunkerModel;
    private static POSModel posModel;
    private static DoccatModel doccatModel;
    private static Set<String> stopWords;
    private static InputStream inputStreamStopWords;
    private static InputStreamFactory inputStreamFactory;
    private static WhitespaceTokenizer tokenizer = WhitespaceTokenizer.INSTANCE;
    private static StanfordCoreNLP pipeline;
    private static Properties properties = new Properties();


    public static CoreNLP getInstance() {
        try {
            loadProps();
            initModels();
            stopWords = new HashSet<>(IOUtils.readLines(inputStreamStopWords, "UTF-8"));
            trainModel();
        } catch (IOException ex) {
            logger.info("Exception occur while trying to initialize ");
        }
        pipeline = new StanfordCoreNLP(setPipelineProperties());
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

    private static void loadProps(){
        InputStream inputStream = CoreNLP.class.getClassLoader().getResourceAsStream("corenlp.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Properties setPipelineProperties() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        return props;
    }

    private static void initModels() {
        try {
            inputStreamStopWords = new FileInputStream(properties.getProperty("corenlp.stopwords"));
            personModel = new TokenNameFinderModel(new FileInputStream(properties.getProperty("corenlp.person.model")));
            locationModel = new TokenNameFinderModel(new FileInputStream(properties.getProperty("corenlp.location.model")));
            organizationModel = new TokenNameFinderModel(new FileInputStream(properties.getProperty("corenlp.organization.model")));
            chunkerModel = new ChunkerModel(new FileInputStream(properties.getProperty("corenlp.chunker.model")));
            posModel = new POSModel(new FileInputStream(properties.getProperty("corenlp.pos.maxent.model")));
            inputStreamFactory = new MarkableFileInputStreamFactory(new File(properties.getProperty("corenlp.training.tweets")));
        } catch (IOException ex) {
            ex.printStackTrace();
            ex.getMessage();
            ex.getCause();
        }
    }

    public static void trainModel() throws IOException {
        ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, "UTF-8");
        ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);
        TrainingParameters trainingParameters = new TrainingParameters();
        //training parameters , but with default params gives best results
        int cutoff = 15;
        int trainingIterations = 300;
        trainingParameters.put("Cutoff", cutoff);
        trainingParameters.put("Iterations", trainingIterations);
        doccatModel = DocumentCategorizerME
                .train("en", sampleStream, TrainingParameters.defaultParams(), new DoccatFactory());
    }

    public String classify(String content) {
        String sentiment;
        String[] tokens = tokenizer.tokenize(content);
        double[] outcomes = categorizer.categorize(tokens);
        String category = categorizer.getBestCategory(outcomes);
        if (category.equalsIgnoreCase("2"))
            sentiment = "POSITIVE";
        else if (category.equalsIgnoreCase("0"))
            sentiment = "NEGATIVE";
        else
            sentiment = "NEUTRAL";

        return sentiment;
    }


    public Set<String> findNEPerson(String content) {
        Set<String> persons = new HashSet<>();
        String[] tokens = tokenizer.tokenize(content);
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
        String[] tokens = tokenizer.tokenize(content);
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
        String[] tokens = tokenizer.tokenize(content);
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
        String[] tokens = tokenizer.tokenize(content);
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
        String clearContent = "";
        for (String word : stopWords)
            clearContent = content.replaceAll("\\s+" + word + "\\s+", " ");

        Map<String, Set<String>> cashtagsAndhastags =
                extractCashTagHashTagAndMentions(Arrays.asList(tokenizer.tokenize(clearContent)));
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

    public static void getSentiment(String text) {

        double sentimentSum = 0;
        int mainSentiment = 0;
        int longest = 0;

        Annotation annotation = pipeline.process(text);
        // mainSentiment is the sentiment of the whole document. We find
        // the whole document by comparing the length of individual
        // annotated "fragments"
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            String partText = sentence.toString();
            if (partText.length() > longest) {
                mainSentiment = sentiment;
                longest = partText.length();
            }
        }

        sentimentSum += mainSentiment;

        double average = sentimentSum / text.length();
        String sentiment;
        if (average >= 2.25)
            sentiment = "POSITIVE";
        else if (average <= 1.75)
            sentiment = "NEGATIVE";
        else
            sentiment = "NEUTRAL";
        System.out.println(mainSentiment);
    }


    public static void main(String[] args) {


    }

}
