package processing;

import model.Document;

import java.io.Serializable;

public class Processing implements Serializable {
    private static Processing INSTANCE;


    public static Processing getInstance() {
        if (INSTANCE == null) {
            synchronized (Processing.class) {
                INSTANCE = new Processing();
            }
        }

        return INSTANCE;
    }

    public Document process(Document document){
        document.getDoc();

        return document;
    }

}
