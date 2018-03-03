package processing;

import analytics.CoreNLP;
import model.Document;
import model.Model;

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

    public Model process(Model model) {

        coreNLP.processWithAnalytics(model);
        return model;
    }
}


