package processing;

import analytics.CoreNLP;
import model.Document;
import java.io.Serializable;

public class Processing implements Serializable {
    private static Processing INSTANCE;
    private static CoreNLP coreNLP;

    static {
        try {
            coreNLP = CoreNLP.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Processing getInstance() {
        if (INSTANCE == null) {
            synchronized (Processing.class) {
                INSTANCE = new Processing();
            }
        }

        return INSTANCE;
    }

    public static Document process(Document document) {
        if (document.getProcess()) coreNLP.processWithAnalytics(document.getModel());
        return document;
    }
}


