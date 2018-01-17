package analytics;

import model.Model;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static ChunkerME chunker;
    private static POSTagger posTagger;
    private static TokenizerModel tokenizerModel;
    private static TokenNameFinderModel personModel;
    private static TokenNameFinderModel locationModel;
    private static TokenNameFinderModel organizationModel;
    private static ChunkerModel chunkerModel;
    private static POSModel posModel;


    public static CoreNLP getInstance() throws Exception {
        initModels();
        tokenizer = new TokenizerME(tokenizerModel);
        personFinder = new NameFinderME(personModel);
        locationFinder = new NameFinderME(locationModel);
        organizationFinder = new NameFinderME(organizationModel);
        chunker = new ChunkerME(chunkerModel);
        posTagger = new POSTaggerME(posModel);

        if (INSTANCE == null) {
            synchronized (CoreNLP.class) {
                INSTANCE = new CoreNLP();
            }
        }
        return INSTANCE;
    }

    private static void initModels() throws IOException {
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

    public static String[] tokenize(String sentence) {
        try {
            return tokenizer.tokenize(sentence);
        }catch (Exception ex){
            ex.getMessage();
            ex.getCause();
        }
        return new String[0];
    }

    public Set<String> findNEPerson(String content) {
        Set<String> persons = new HashSet<>();
        String[] tokens = tokenize(content);
        if(tokens.length > 0) {
            Span[] nameSpans = personFinder.find(tokens);
            String[] namedEntities = (Span.spansToStrings(nameSpans, tokens));
            for (String entity : namedEntities) persons.add(entity);

            if (!persons.isEmpty())
                logger.info("Person named entities found " + persons);
            else
                logger.info("No named entities person found !");
            return persons;
        }
        return new HashSet<>();
    }

    public Set<String> findNELocation(String content) {
        Set<String> locations = new HashSet<>();
        String[] tokens = tokenize(content);
        if(tokens.length > 0) {
            Span[] nameSpans = locationFinder.find(tokens);
            String[] namedEntities = (Span.spansToStrings(nameSpans, tokens));
            for (String entity : namedEntities) {
                locations.add(entity);
            }
            if (!locations.isEmpty())
                logger.info("Named entities of type location found  " + locations);
            else
                logger.info("No named entities of type location found !");
            return locations;
        }
        return new HashSet<>();
    }

    public Set<String> findNEOrganization(String content) {
        Set<String> organizations = new HashSet<>();
        String[] tokens = tokenize(content);
        if(tokens.length > 0) {
            Span[] nameSpans = organizationFinder.find(tokens);
            String[] namedEntities = (Span.spansToStrings(nameSpans, tokens));
            for (String entity : namedEntities) {
                organizations.add(entity);
            }

            if (!organizations.isEmpty())
                logger.info("Named entities of type location found  " + organizations);
            else
                logger.info("No named entities of type location found !");
            return organizations;
        }
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
                .filter(token->token.startsWith("$") && !StringUtils.isNumeric(token.substring(1)) && !token.contains("."))
                .collect(Collectors.toSet());

        Set<String> mentions = tokens.stream()
                .distinct()
                .filter(s -> s.startsWith("@"))
                .collect(Collectors.toSet());
        mentions.forEach(s -> {
            if (s.contains(":")) s = s.substring(0, s.indexOf(":"));
        });

        extractedCashTagHashTagsAndMentions.put("@" , mentions);
        extractedCashTagHashTagsAndMentions.put("#", hashtags);
        extractedCashTagHashTagsAndMentions.put("$", cashtags);

        return extractedCashTagHashTagsAndMentions;
    }

    public void processWithAnalytics(Model model) {
        String content = model.getContent();
        Map<String,Set<String>> cashtagsAndhastags = extractCashTagHashTagAndMentions(Arrays.asList(tokenize(content)));
        Object mentions = cashtagsAndhastags.values().toArray()[0];
        Object hashtags = cashtagsAndhastags.values().toArray()[1];
        Object cashtags = cashtagsAndhastags.values().toArray()[2];
        model.setPersons(findNEPerson(content));
        model.setOrganizations(findNEOrganization(content));
        model.setLocations(findNELocation(content));
        model.setHashtags((Set<String>) hashtags);
        model.setCashtags((Set<String>) cashtags);
        model.setMentions((Set<String>) mentions);

    }

    public static void main(String[] args) {
        try {
            CoreNLP.getInstance().chunker("Most large cities in the world had morning and afternoon newspapers");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
