package model;

import lombok.Data;

import java.io.Serializable;

public @Data
class Document implements Serializable {

    private Document document;
    private String content;
    private String userName;


    public Document(Document document){
        this.document = document;
    }
    public Document getDoc(){
        return this.document;
    }


}
