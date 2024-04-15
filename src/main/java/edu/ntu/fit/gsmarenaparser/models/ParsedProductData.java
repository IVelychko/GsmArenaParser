package edu.ntu.fit.gsmarenaparser.models;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class ParsedProductData implements Serializable {
    private Map<String, Map<String, String>> content;
    public Map<String, Map<String, String>> getContent() {
        return content;
    }
    public void setContent(Map<String, Map<String, String>> content) {
        this.content = content;
    }
    public ParsedProductData() {
        content = new LinkedHashMap<>();
    }
}
