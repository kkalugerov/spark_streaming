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

    public Document process(Document document) {
        boolean process = document.getProcess();
        if (process)
            coreNLP.processWithAnalytics(document.getModel());
        return document;
    }
}


