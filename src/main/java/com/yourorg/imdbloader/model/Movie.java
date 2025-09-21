package com.yourorg.imdbloader.model;

import java.util.List;

public class Movie {
    private String id;
    private String primaryTitle;
    private List<String> genres;
    private List<String> actors;
    private int year;
    private double rating;
    private int votes;
    private String plot;
    private double score;

    public Movie() {}

    public Movie(String id, String primaryTitle, List<String> genres, List<String> actors, 
                 int year, double rating, int votes, String plot, double score) {
        this.id = id;
        this.primaryTitle = primaryTitle;
        this.genres = genres;
        this.actors = actors;
        this.year = year;
        this.rating = rating;
        this.votes = votes;
        this.plot = plot;
        this.score = score;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrimaryTitle() {
        return primaryTitle;
    }

    public void setPrimaryTitle(String primaryTitle) {
        this.primaryTitle = primaryTitle;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getActors() {
        return actors;
    }

    public void setActors(List<String> actors) {
        this.actors = actors;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id='" + id + '\'' +
                ", primaryTitle='" + primaryTitle + '\'' +
                ", genres=" + genres +
                ", actors=" + actors +
                ", year=" + year +
                ", rating=" + rating +
                ", votes=" + votes +
                ", plot='" + plot + '\'' +
                ", score=" + score +
                '}';
    }
}
