import analytics.CoreNLP;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class CoreNLPTest {

    private CoreNLP coreNLP = CoreNLP.getInstance();


    private List<String> sentences = Arrays.asList("The painting is ugly, will return it tomorrow...",
            "Bad news, my flight just got cancelled.", "I lost my keys", "He killed our good mood",
            "I feel bad for what I did", "I hate to say goodbye", "Had a bad evening, need urgently a beer.",
            "Watching a nice movie", "One of the best soccer games, worth seeing it",
            "Very tasty, not only for vegetarians", "On today's show we met Angela, a woman with an amazing story",
            "Love the new book I reveived for Christmas", "Thank you Molly making this possible",
            "Too early to travel..need a coffee", "I put on weight again", "On a trip to Iceland",
            "I set my new year's resolution", "Sorry mate, there is no more room for you",
            "Nobody to ask about directions", "I fell in love again");

    @Test
    public void testSentiment() {
        for (String sentece : sentences) {
            String sentiment = coreNLP.classify(sentece);
            assertThat(sentiment, anyOf(is("NEGATIVE"), is("POSITIVE"), is("NEUTRAL")));
        }

    }

}
