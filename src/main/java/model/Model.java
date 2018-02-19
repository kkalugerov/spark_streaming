package model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public @Data class Model implements Serializable {
    private String rawJson;
    private String sentiment;
    private String content;
    private String lang;
    private String origin;
    private List<String> keywords;
    private Set<String> locations = new HashSet<>();
    private Set<String> hashtags = new HashSet<>();
    private Set<String> mentions = new HashSet<>();
    private Set<String> cashtags = new HashSet<>();
    private Set<String> persons = new HashSet<>();
    private Set<String> organizations = new HashSet<>();

    public Model() {
    }

}