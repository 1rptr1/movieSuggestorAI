# IMDB Data Loader & Movie Suggestor

This Spring Boot application automatically loads IMDB data and provides intelligent movie recommendations with user preference tracking.

## 🚀 Features

- **Automatic IMDB Data Loading**: Loads TSV files from IMDB datasets on startup
- **Database Health Monitoring**: Checks and creates required tables automatically  
- **Movie Suggestion Engine**: AI-powered recommendations based on user preferences
- **JSONB User Preferences**: Flexible preference storage using PostgreSQL JSONB
- **RESTful API**: Complete REST endpoints for movie operations and suggestions

## 📋 Prerequisites

1. **PostgreSQL Database** (version 12+)
2. **Java 17+**
3. **IMDB Dataset Files** (TSV format)

## 🛠️ Setup Instructions

### 1. Database Setup
```sql
-- Create database
CREATE DATABASE imdb;

-- The application will automatically create all required tables
```

### 2. Download IMDB Data
Download these files from [IMDB Datasets](https://datasets.imdbws.com/):
- `name.basics.tsv.gz`
- `title.basics.tsv.gz` 
- `title.principals.tsv.gz`
- `title.akas.tsv.gz`

Extract them to a directory (e.g., `C:/imdb-data/`)

### 3. Configure Application
Edit `src/main/resources/application.properties`:

```properties
# Database connection
spring.datasource.url=jdbc:postgresql://localhost:5432/imdb
spring.datasource.username=postgres
spring.datasource.password=your_password

# IMDB Data Loading
imdb.data.auto-load=true
imdb.data.directory=C:/path/to/your/imdb-data
```

### 4. Run Application
```bash
./mvnw spring-boot:run
```

## 📊 Database Schema

The application creates these tables automatically:

### IMDB Tables
- `name_basics` - Person information (actors, directors, etc.)
- `title_basics` - Movie/TV show basic information
- `title_principals` - Cast and crew information
- `title_akas` - Alternative titles and translations

### Application Tables  
- `user_profiles` - User preferences (JSONB format)
- `user_preferences` - User feedback on movies

## 🔌 API Endpoints

### Movie Operations
```http
GET /api/movies                    # Get all movies
GET /api/movies/{id}              # Get movie by ID
GET /api/movies/search?query=...  # Search movies
```

### Suggestion System
```http
POST /api/suggest/start           # Start suggestion session
POST /api/suggest/feedback        # Provide user feedback  
GET /api/suggest/{userId}         # Get recommendations
```

## 💡 Usage Examples

### Starting a Suggestion Session
```bash
curl -X POST http://localhost:8080/api/suggest/start \
  -H "Content-Type: application/json" \
  -d '{"query": "action movies with superheroes"}'
```

### Providing Feedback
```bash
curl -X POST http://localhost:8080/api/suggest/feedback \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "likedMovieIds": ["tt0468569", "tt1375666"]
  }'
```

### Getting Recommendations
```bash
curl http://localhost:8080/api/suggest/user-123
```

## 🎯 User Preferences System

The application uses JSONB to store flexible user preferences:

```java
// Example preference structure
{
  "initialQuery": "action movies",
  "preferredGenres": ["Action", "Sci-Fi", "Thriller"],
  "preferredActors": ["Tom Hanks", "Leonardo DiCaprio"],
  "minRating": 7.0,
  "preferredDecades": ["2000s", "2010s"],
  "watchedMovies": ["tt0468569", "tt1375666"]
}
```

### Programmatic Usage
```java
@Autowired
private SuggestService suggestService;

// Update user preferences
Map<String, Object> prefs = new HashMap<>();
prefs.put("preferredGenres", Arrays.asList("Action", "Sci-Fi"));
prefs.put("minRating", 8.0);
suggestService.addOrUpdateUserPreferences(userId, prefs);

// Get specific preferences
List<String> genres = suggestService.getUserGenrePreferences(userId);
```

## ⚙️ Configuration Options

| Property | Default | Description |
|----------|---------|-------------|
| `imdb.data.auto-load` | `false` | Enable automatic data loading on startup |
| `imdb.data.directory` | - | Path to directory containing IMDB TSV files |
| `spring.jpa.show-sql` | `true` | Show SQL queries in logs |
| `logging.level.com.yourorg.imdbloader` | `INFO` | Application log level |

## 🔧 Architecture Components

### Core Services
- **ImdbLoaderService**: Handles TSV parsing and batch database inserts
- **SuggestService**: Provides movie recommendations and preference management
- **MovieService**: Basic movie CRUD operations

### Configuration Classes
- **DatabaseInitializer**: Creates tables and loads IMDB data
- **StartupRunner**: Orchestrates application startup and data loading
- **DatabaseHealthChecker**: Monitors database state and table existence

### Data Models
- **UserProfileEntity**: User profiles with JSONB preferences
- **UserPreferenceEntity**: User feedback on specific movies
- **Movie**: Movie data model for API responses

## 🚨 Troubleshooting

### Common Issues

**1. Missing IMDB Files**
```
⚠️ Missing required file: /path/to/name.basics.tsv
```
**Solution**: Ensure all 4 TSV files are present and properly named.

**2. Database Connection Failed**
```
❌ Failed to initialize database
```
**Solution**: Check PostgreSQL is running and credentials are correct.

**3. Out of Memory During Loading**
```
java.lang.OutOfMemoryError
```
**Solution**: Increase JVM heap size: `-Xmx4g -Xms2g`

### Performance Tips

1. **Increase Batch Size**: Modify `BATCH_SIZE` in `ImdbLoaderService` for faster loading
2. **Database Tuning**: Increase PostgreSQL `shared_buffers` and `work_mem`
3. **Parallel Loading**: Consider loading different tables in parallel for large datasets

## 📈 Monitoring

The application provides detailed logging during startup:

```
🚀 Starting IMDB Loader Application...
📁 Using IMDB data directory: /path/to/data
✅ Found: name.basics.tsv
✅ Found: title.basics.tsv
📋 Creating database tables...
✅ All database tables created successfully
🎉 IMDB data loaded successfully!
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.
