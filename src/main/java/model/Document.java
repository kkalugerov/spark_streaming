package model;

import lombok.Data;

import java.io.Serializable;

public class Document implements Serializable {
    private Model model;
    private boolean process;

    public Document() {
    }

    public Document(Model model, boolean process) {
        this.model = model;
        this.process = process;
    }

    public Model getModel() {
        return this.model;
    }

    public boolean getProcess() {
        return this.process;
    }

}
