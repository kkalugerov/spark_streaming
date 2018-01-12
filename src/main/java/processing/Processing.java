package processing;

import analytics.BasicPipeline;
import model.Document;

import java.io.Serializable;

public class Processing implements Serializable {
    private static Processing INSTANCE;
    private static BasicPipeline basicPipeline = BasicPipeline.getInstance();

    public static Processing getInstance() {
        if (INSTANCE == null) {
            synchronized (Processing.class) {
                INSTANCE = new Processing();
            }
        }

        return INSTANCE;
    }

    public Document process(Document document) {
        if(document.getProcess()) {
            String content = document.getModel().getContent();
            document.getModel().setKeywords(basicPipeline.getKeywords(content));
            document.getModel().setLocations(basicPipeline.findNamedEntities(content).get("Locations"));
            document.getModel().setOrganizations(basicPipeline.findNamedEntities(content).get("Organizations"));
            document.getModel().setPersons(basicPipeline.findNamedEntities(content).get("Person"));
        }

        return document;
    }


}
