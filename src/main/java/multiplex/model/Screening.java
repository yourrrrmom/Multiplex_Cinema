
package multiplex.model;

public class Screening {
    private int screeningId;
    private int movieId;
    private int hallId;
    private String startTime;
    private double ticketPrice;

    public Screening(int screeningId, int movieId, int hallId, String startTime, double ticketPrice) {
        this.screeningId = screeningId;
        this.movieId = movieId;
        this.hallId = hallId;
        this.startTime = startTime;
        this.ticketPrice = ticketPrice;
    }

    // Getters & Setters
    public int getScreeningId() { return screeningId; }
    public int getMovieId() { return movieId; }
    public int getHallId() { return hallId; }
    public String getStartTime() { return startTime; }
    public double getTicketPrice() { return ticketPrice; }
    public void setScreeningId(int screeningId) { this.screeningId = screeningId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public void setHallId(int hallId) { this.hallId = hallId; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }
    
}
