
package multiplex.model;

public class Reservation {
    private int reservationId;
    private int screeningId;
    private int createdByUserId;
    private String customerName;
    private double totalPrice;
    private String paymentStatus;
    private String createdAt;

    public Reservation(int reservationId, int screeningId, int createdByUserId, String customerName, double totalPrice, String paymentStatus, String createdAt) {
        this.reservationId = reservationId;
        this.screeningId = screeningId;
        this.createdByUserId = createdByUserId;
        this.customerName = customerName;
        this.totalPrice = totalPrice;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public int getReservationId() { return reservationId; }
    public int getScreeningId() { return screeningId; }
    public int getCreatedByUserId() { return createdByUserId; }
    public String getCustomerName() { return customerName; }
    public double getTotalPrice() { return totalPrice; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getCreatedAt() { return createdAt; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }
    public void setScreeningId(int screeningId) { this.screeningId = screeningId; }
    public void setCreatedByUserId(int createdByUserId) { this.createdByUserId = createdByUserId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setCreatedAt(String createdAt) {  this.createdAt = createdAt; }
    
}
