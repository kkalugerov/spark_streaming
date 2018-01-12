package analytics;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CoreNLP {
    private static final Logger LOG = LoggerFactory.getLogger(CoreNLP.class);

    public static void main(String[] args) throws IOException {
        PrintWriter out;
        if (args.length > 1) {
            out = new PrintWriter(args[1]);
        } else {
            out = new PrintWriter(System.out);
        }
        PrintWriter xmlOut = null;
        if (args.length > 2) {
            xmlOut = new PrintWriter(args[2]);
        }

        StanfordCoreNLP pipeline = new StanfordCoreNLP();
        Annotation annotation;
        if (args.length > 0) {
            annotation = new Annotation(IOUtils.slurpFileNoExceptions(args[0]));
        } else {
            annotation = new Annotation("Kosgi Santosh sent an email to Stanford University. He didn't get a reply.");
        }

        pipeline.annotate(annotation);
        pipeline.prettyPrint(annotation, out);
        if (xmlOut != null) {
            pipeline.xmlPrint(annotation, xmlOut);
        }
        // An Annotation is a Map and you can get and use the various analyses individually.
        // For instance, this gets the parse tree of the first sentence in the text.
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences != null && sentences.size() > 0) {
            CoreMap sentence = sentences.get(0);
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            out.println();
            out.println("The first sentence parsed is:");
            tree.pennPrint(out);
        }
    }
//    @Test
//    public void basic() {
//        LOG.debug("Starting Stanford NLP");
//
//        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and
//        Properties props = new Properties();
//        boolean useRegexner = true;
//        if (useRegexner) {
//            props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");
//            props.setProperty("ner.useSUTime", "0");
//        } else {
//            props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
//        }
//        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//
//
//        // // We're interested in NER for these things (jt->loc->sal)
//        String[] tests =
//                {
//                        "Partial invoice (€100,000, so roughly 40%) for the consignment C27655 we shipped on 15th August to London from the Make Believe Town depot. INV2345 is for the balance.. Customer contact (Sigourney) says they will pay this on the usual credit terms (30 days)."
//                };
//        List tokens = new ArrayList<>();
//
//        for (String s : tests) {
//
//            // run all Annotators on the passed-in text
//            Annotation document = new Annotation(s);
//            pipeline.annotate(document);
//
//            // these are all the sentences in this document
//            // a CoreMap is essentially a Map that uses class objects as keys and has values with
//            // custom types
//            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
//            StringBuilder sb = new StringBuilder();
//
//            //I don't know why I can't get this code out of the box from StanfordNLP, multi-token entities
//            //are far more interesting and useful..
//            //TODO make this code simpler..
//            for (CoreMap sentence : sentences) {
//                // traversing the words in the current sentence, "O" is a sensible default to initialise
//                // tokens to since we're not interested in unclassified / unknown things..
//                String prevNeToken = "O";
//                String currNeToken = "O";
//                boolean newToken = true;
//                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                    currNeToken = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//                    String word = token.get(CoreAnnotations.TextAnnotation.class);
//                    // Strip out "O"s completely, makes code below easier to understand
//                    if (currNeToken.equals("O")) {
//                        // LOG.debug("Skipping '{}' classified as {}", word, currNeToken);
//                        if (!prevNeToken.equals("O") && (sb.length() > 0)) {
//                            handleEntity(prevNeToken, sb, tokens);
//                            newToken = true;
//                        }
//                        continue;
//                    }
//
//                    if (newToken) {
//                        prevNeToken = currNeToken;
//                        newToken = false;
//                        sb.append(word);
//                        continue;
//                    }
//
//                    if (currNeToken.equals(prevNeToken)) {
//                        sb.append(" " + word);
//                    } else {
//                        // We're done with the current entity - print it out and reset
//                        // TODO save this token into an appropriate ADT to return for useful processing..
//                        handleEntity(prevNeToken, sb, tokens);
//                        newToken = true;
//                    }
//                    prevNeToken = currNeToken;
//                }
//            }
//
//            //TODO - do some cool stuff with these tokens!
//            LOG.debug("We extracted {} tokens of interest from the input text", tokens.size());
//        }
//    }
//    private void handleEntity(String inKey, StringBuilder inSb, List inTokens) {
//        LOG.debug("'{}' is a {}", inSb, inKey);
//        inTokens.add(new EmbeddedToken(inKey, inSb.toString()));
//        inSb.setLength(0);
//    }
//
//
//}
//class EmbeddedToken {
//
//    private String name;
//    private String value;
//
//    public String getName() {
//        return name;
//    }
//
//    public String getValue() {
//        return value;
//    }
//
//    public EmbeddedToken(String name, String value) {
//        super();
//        this.name = name;
//        this.value = value;
//    }
//    public static void main(String[] args) {
//        Properties pipelineProps = new Properties();
//        Properties tokenizerProps = new Properties();
//        pipelineProps.setProperty("annotators", "parse, sentiment");
//        pipelineProps.setProperty("parse.binaryTrees", "true");
//        pipelineProps.setProperty("enforceRequirements", "false");
//        tokenizerProps.setProperty("annotators", "tokenize ssplit");
//        StanfordCoreNLP tokenizer = new StanfordCoreNLP(tokenizerProps);
//        StanfordCoreNLP pipeline = new StanfordCoreNLP(pipelineProps);
//        String line = "Amazingly grateful beautiful friends are fulfilling an incredibly joyful accomplishment. What an truly terrible idea.";
//        Annotation annotation = tokenizer.process(line);
//        pipeline.annotate(annotation);
//        // normal output
//        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
//            String output = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
//            System.out.println(output);
//        }
//    }
}
