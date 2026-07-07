package model;

public class ReservationDisplay {
    private final int reservationId;
    private final String movieDetails; // Ταινία + Ώρα
    private final double totalPrice;

    public ReservationDisplay(int reservationId, String movieDetails, double totalPrice) {
        this.reservationId = reservationId;
        this.movieDetails = movieDetails;
        this.totalPrice = totalPrice;
    }

    public int getReservationId() { return reservationId;}
    public String getMovieDetails() { return movieDetails;}
    public double getTotalPrice() { return totalPrice;}
}
