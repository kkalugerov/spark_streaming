package analytics;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;


public class BasicPipeline {
    public static void main(String[] args) {
        PrintWriter out = null ;
        out = new PrintWriter(System.out);
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String test_text = "Worldpay bietet gemeinsam mit iyzico Zahlungsabwicklung in der Türkei";
        Annotation annotation = pipeline.process(test_text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences){
            System.out.println(sentence.get(SentimentCoreAnnotations.SentimentClass.class));
        }

        //        int longest = 0;
//        int mainSentiment = 0;
//        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
//            Tree tree = sentence.get(SentimentCoreAnnotations.class);
//            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
//            String partText = sentence.toShorterString();
//            if (partText.length() > longest){
//                mainSentiment = sentiment;
//                longest = partText.length();
//            }
//            System.out.println(mainSentiment);
//
//        }
    }
}
