package edu.ntu.fit.gsmarenaparser.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ntu.fit.gsmarenaparser.models.ParsedProductData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JsonSerializerService {
    @Autowired
    private ObjectMapper objectMapper;
    public String serializeToJson(ParsedProductData parsedContent) {
        try {
            return objectMapper.writeValueAsString(parsedContent);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    public ParsedProductData deserializeFromJson(String jsonContent) {
        try {
            return objectMapper.readValue(jsonContent, ParsedProductData.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
