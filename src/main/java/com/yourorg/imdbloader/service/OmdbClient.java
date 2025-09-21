package com.yourorg.imdbloader.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OmdbClient {

    private static final String API_URL = "http://www.omdbapi.com/?apikey=YOUR_API_KEY&i=";
    private static final Logger log = LoggerFactory.getLogger(OmdbClient.class);

    public String fetchPlotById(String imdbId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(API_URL + imdbId, String.class);
            JsonNode json = new ObjectMapper().readTree(response);
            
            if (json.has("Plot") && !json.get("Plot").asText().equals("N/A")) {
                return json.get("Plot").asText();
            } else {
                log.warn("No plot available for movie with ID: {}", imdbId);
                return "Plot not available";
            }
        } catch (Exception e) {
            log.error("Error fetching plot for movie ID {}: {}", imdbId, e.getMessage());
            return "Plot not available";
        }
    }
}
