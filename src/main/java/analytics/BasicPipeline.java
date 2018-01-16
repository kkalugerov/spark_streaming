package analytics;


import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.apache.log4j.Logger;
import processing.Processing;

import java.io.PrintWriter;
import java.util.*;


public class BasicPipeline {
//    private static Properties properties = (Properties) new Properties()
//            .setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");
//    private static StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
    private static BasicPipeline INSTANCE;
    private static Logger logger = Logger.getLogger(BasicPipeline.class);


    public static BasicPipeline getInstance() {
        if (INSTANCE == null) {
            synchronized (BasicPipeline.class) {
                INSTANCE = new BasicPipeline();
            }
        }
        return INSTANCE;
    }


    public List<String> getKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        String[] words = text.split(" ");
        for (String word : words) {
            if (word.length() > 4) {
                keywords.add(word);
            }
//            return keywords;
        }
        return keywords;
    }


//    public  Map<String, Set<String>> findNamedEntities(String text) {
//        logger.info("Finding entity type matches in the " + text);
//
//        // create an empty Annotation just with the given text
//        Annotation document = new Annotation(text);
//
//        // run all Annotators on this text
//        pipeline.annotate(document);
//        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
//        Map<String, Set<String>> entities = new HashMap<>();
//        Set<String> person = new HashSet<>();
//        Set<String> locations = new HashSet<>();
//        Set<String> organization = new HashSet<>();
//        for (CoreMap sentence : sentences) {
//            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                String word = token.get(CoreAnnotations.TextAnnotation.class);
//                String entityType = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//
//                if (entityType.equalsIgnoreCase("PERSON"))
//                    person.add(word);
//                if (entityType.equalsIgnoreCase("LOCATION"))
//                    locations.add(word);
//                if (entityType.equalsIgnoreCase("ORGANIZATION"))
//                    organization.add(word);
//            }
//        }
//
//        entities.put("Persons", person);
//        entities.put("Locations", locations);
//        entities.put("Organizations", organization);
//
//        return entities;
//    }
}





