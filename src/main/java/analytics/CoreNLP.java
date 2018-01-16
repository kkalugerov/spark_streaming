package analytics;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class CoreNLP {
    private static final Logger logger = LoggerFactory.getLogger(CoreNLP.class);
    private static CoreNLP INSTANCE;
    private static String fileToken = new File("").getAbsolutePath() + "/src/main/resources/en-token.bin";
    private static String filePerson = new File("")
            .getAbsolutePath() + "/src/main/resources/en-ner-person.bin";
    private static String fileLocation = new File("")
            .getAbsolutePath() + "/src/main/resources/en-ner-location.bin";
    private static String fileOrganization = new File("")
            .getAbsolutePath() + "/src/main/resources/en-ner-organization.bin";
    private static TokenizerModel tokenizerModel;
    private static TokenNameFinderModel personModel;
    private static TokenNameFinderModel locationModel;
    private static TokenNameFinderModel organizationModel;

    public CoreNLP() {

    }

    public static CoreNLP getInstance() throws Exception {
        FileInputStream inputStreamTokenizer = new FileInputStream(new File(fileToken));
        FileInputStream inputStreamPerson = new FileInputStream(new File(filePerson));
        FileInputStream inputStreamLocation = new FileInputStream(new File(fileLocation));
        FileInputStream inputStreamOrganization = new FileInputStream(new File(fileOrganization));
        tokenizerModel = new TokenizerModel(inputStreamTokenizer);
        personModel = new TokenNameFinderModel(inputStreamPerson);
        locationModel = new TokenNameFinderModel(inputStreamLocation);
        organizationModel = new TokenNameFinderModel(inputStreamOrganization);
        if (INSTANCE == null) {
            synchronized (CoreNLP.class) {
                INSTANCE = new CoreNLP();
            }
        }
        return INSTANCE;
    }

    public String[] tokenize(String sentence) {
        TokenizerME tokenizer = new TokenizerME(tokenizerModel);
        return tokenizer.tokenize(sentence);
    }

    public Set<String> findNEPerson(String content) {
        Set<String> persons = new HashSet<>();
        String[] tokens = tokenize(content);
        Span[] nameSpans = new NameFinderME(personModel).find(tokens);
        String[] namedEntities = (Span.spansToStrings(nameSpans, tokens));
        for (String entity : namedEntities) persons.add(entity);

        if (!persons.isEmpty())
            logger.info("Person named entities found " + persons);
        else
            logger.info("No named entities person found !");
        return persons;
    }

    public Set<String> findNELocation(String content) {
        Set<String> locations = new HashSet<>();
        String[] tokens = tokenize(content);
        Span[] nameSpans = new NameFinderME(locationModel).find(tokens);
        String[] namedEntities = (Span.spansToStrings(nameSpans, tokens));
        for (String entity : namedEntities) {
            locations.add(entity);
        }
        return locations;
    }

    public Set<String> findNEOrganization(String content) {
        Set<String> organizations = new HashSet<>();
        String[] tokens = tokenize(content);
        Span[] nameSpans =  new NameFinderME(organizationModel).find(tokens);
        String[] namedEntities = (Span.spansToStrings(nameSpans, tokens));
        for (String entity : namedEntities) {
            organizations.add(entity);
        }
        return organizations;
    }

    public static void main(String[] args) {

    }

}
