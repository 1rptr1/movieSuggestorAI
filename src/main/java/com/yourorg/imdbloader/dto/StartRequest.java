package com.yourorg.imdbloader.dto;

public class StartRequest {
    private String query; // e.g. "action movies", "romantic comedy"

    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
}
