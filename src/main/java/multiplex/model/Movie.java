
package multiplex.model;

public class Movie {
    private int movieId;
    private String title;
    private String description;
    private int duration;
    private String genre;
    private String posterPath;

    public Movie(int movieId, String title, String description, int duration, String genre, String posterPath) {
        this.movieId = movieId;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.genre = genre;
        this.posterPath = posterPath;
    }

    // Getters & Setters 
    public int getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getDuration() { return duration; }
    public String getGenre() { return genre; }
    public String getPosterPath() { return posterPath; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

}
