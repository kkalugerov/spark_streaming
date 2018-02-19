package analytics;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.Set;


public final class StemmAnalyzer extends Analyzer {
    private static final Set<?> STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

    protected final Version matchVersion;
    private final Set<?> stopWords;


    public StemmAnalyzer(Version version){
        this(version,null);
    }

    public StemmAnalyzer(Version version,Set<?> stopWords){
        this.matchVersion = version;
        this.stopWords = stopWords;
    }

    public CharArraySet getStopWords() {
        return stopWords == null ? CharArraySet.copy(STOP_WORDS_SET) : CharArraySet.copy(stopWords);
    }

    @Override
    protected TokenStreamComponents createComponents(String s, Reader reader) {
        return null;
    }

    public TokenStream tokenStreams(String fieldName, Reader reader) {
        final StandardTokenizer src = new StandardTokenizer(matchVersion, reader);
        TokenStream tok = new StandardFilter(matchVersion, src);
        tok = new LowerCaseFilter(matchVersion, tok);
        tok = new StopFilter(matchVersion, tok,  getStopWords());
        return tok;
    }

}
