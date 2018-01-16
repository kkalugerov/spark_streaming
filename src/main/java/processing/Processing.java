package processing;

import analytics.BasicPipeline;
import analytics.CoreNLP;
import model.Document;

import java.io.FileNotFoundException;
import java.io.IOException;
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
        if (document.getProcess()) {
            String content = document.getModel().getContent();

            document.getModel().setPersons(coreNLP.findNEPerson(content));
            document.getModel().setLocations(coreNLP.findNELocation(content));
            document.getModel().setOrganizations(coreNLP.findNEOrganization(content));

//            document.getModel().setKeywords(basicPipeline.getKeywords(content));
//            document.getModel().setLocations(basicPipeline.findNamedEntities(content).get("Locations"));
//            document.getModel().setOrganizations(basicPipeline.findNamedEntities(content).get("Organizations"));
//            document.getModel().setPersons(basicPipeline.findNamedEntities(content).get("Person"));


        }
        return document;
    }
}


