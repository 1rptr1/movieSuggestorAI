package com.yourorg.imdbloader.service;

import com.yourorg.imdbloader.dto.MovieDto;
import com.yourorg.imdbloader.model.Movie;
import com.yourorg.imdbloader.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final OmdbClient omdbClient;
    private static final Logger log = LoggerFactory.getLogger(MovieService.class);

    public MovieService(MovieRepository movieRepository, OmdbClient omdbClient) {
        this.movieRepository = movieRepository;
        this.omdbClient = omdbClient;
    }

    public void testLogging() {
        log.info("This is an INFO log");
        log.warn("This is a WARN log");
        log.error("This is an ERROR log");
    }

    /**
     * Returns top movies by actor with plots fetched from OMDb.
     */
    public List<MovieDto> getTopMoviesWithPlot(String actor, int limit) {
        log.info("Fetching top {} movies for actor: {}", limit, actor);
        List<Map<String, Object>> results = movieRepository.findTopMoviesByActor(actor, limit);
        log.info("Found {} movies for actor: {}", results.size(), actor);
        
        return results.stream()
                .map(row -> {
                    String title = (String) row.get("title");
                    double rating = (double) row.getOrDefault("rating", 0.0);
                    int votes = ((Number) row.getOrDefault("votes", 0)).intValue();
                    String imdbId = (String) row.get("tconst");

                    log.info("Processing movie: {} ({})", title, imdbId);

                    String plot = omdbClient.fetchPlotById(imdbId);

                    return new MovieDto(title, rating, votes, plot);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all movies - mock implementation
     */
    public List<Movie> getAllMovies() {
        log.info("Fetching all movies");
        return getMockMovies();
    }

    /**
     * Get movie by ID - mock implementation
     */
    public Movie getMovieById(String id) {
        log.info("Fetching movie with ID: {}", id);
        return getMockMovies().stream()
                .filter(movie -> movie.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Search movies by query - mock implementation
     */
    public List<Movie> searchMovies(String query) {
        log.info("Searching movies with query: {}", query);
        List<Movie> allMovies = getMockMovies();
        
        return allMovies.stream()
                .filter(movie -> 
                    movie.getPrimaryTitle().toLowerCase().contains(query.toLowerCase()) ||
                    movie.getGenres().stream().anyMatch(genre -> genre.toLowerCase().contains(query.toLowerCase())) ||
                    movie.getActors().stream().anyMatch(actor -> actor.toLowerCase().contains(query.toLowerCase()))
                )
                .collect(Collectors.toList());
    }

    /**
     * Mock data for demonstration purposes
     */
    private List<Movie> getMockMovies() {
        return Arrays.asList(
            new Movie("tt0468569", "The Dark Knight", 
                Arrays.asList("Action", "Crime", "Drama"), 
                Arrays.asList("Christian Bale", "Heath Ledger", "Aaron Eckhart"),
                2008, 9.0, 2500000, "Batman battles the Joker in Gotham City", 9.0),
            
            new Movie("tt1375666", "Inception", 
                Arrays.asList("Action", "Sci-Fi", "Thriller"), 
                Arrays.asList("Leonardo DiCaprio", "Marion Cotillard", "Tom Hardy"),
                2010, 8.8, 2200000, "A thief enters people's dreams to steal secrets", 8.8),
            
            new Movie("tt0816692", "Interstellar", 
                Arrays.asList("Adventure", "Drama", "Sci-Fi"), 
                Arrays.asList("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain"),
                2014, 8.6, 1600000, "Astronauts travel through a wormhole to save humanity", 8.6),
            
            new Movie("tt0111161", "The Shawshank Redemption", 
                Arrays.asList("Drama"), 
                Arrays.asList("Tim Robbins", "Morgan Freeman"),
                1994, 9.3, 2400000, "Two imprisoned men bond over years and find redemption", 9.3),
            
            new Movie("tt0137523", "Fight Club", 
                Arrays.asList("Drama"), 
                Arrays.asList("Brad Pitt", "Edward Norton", "Helena Bonham Carter"),
                1999, 8.8, 1900000, "An insomniac office worker forms an underground fight club", 8.8),
            
            new Movie("tt0109830", "Forrest Gump", 
                Arrays.asList("Drama", "Romance"), 
                Arrays.asList("Tom Hanks", "Robin Wright", "Gary Sinise"),
                1994, 8.8, 1800000, "The presidencies of Kennedy and Johnson through the eyes of an Alabama man", 8.8),
            
            new Movie("tt0110912", "Pulp Fiction", 
                Arrays.asList("Crime", "Drama"), 
                Arrays.asList("John Travolta", "Uma Thurman", "Samuel L. Jackson"),
                1994, 8.9, 1900000, "The lives of two mob hitmen, a boxer, and others intertwine", 8.9),
            
            new Movie("tt0167260", "The Lord of the Rings: The Return of the King", 
                Arrays.asList("Action", "Adventure", "Drama"), 
                Arrays.asList("Elijah Wood", "Viggo Mortensen", "Ian McKellen"),
                2003, 8.9, 1700000, "Gandalf and Aragorn lead the World of Men against Sauron's army", 8.9),
            
            new Movie("tt0120737", "The Lord of the Rings: The Fellowship of the Ring", 
                Arrays.asList("Action", "Adventure", "Drama"), 
                Arrays.asList("Elijah Wood", "Ian McKellen", "Orlando Bloom"),
                2001, 8.8, 1700000, "A meek Hobbit and eight companions set out to destroy the One Ring", 8.8),
            
            new Movie("tt0073486", "One Flew Over the Cuckoo's Nest", 
                Arrays.asList("Drama"), 
                Arrays.asList("Jack Nicholson", "Louise Fletcher", "Danny DeVito"),
                1975, 8.7, 950000, "A criminal pleads insanity and is admitted to a mental institution", 8.7)
        );
    }
}
