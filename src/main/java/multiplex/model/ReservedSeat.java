
package multiplex.model;

public class ReservedSeat {
    private int reservationId;
    private int screeningId;
    private int rowNum;
    private int colNum;

    public ReservedSeat(int reservationId, int screeningId, int rowNum, int colNum) {
        this.reservationId = reservationId;
        this.screeningId = screeningId;
        this.rowNum = rowNum;
        this.colNum = colNum;
    }

    // Getters & Setters
    public int getReservationId() { return reservationId; }
    public int getScreeningId() { return screeningId; }
    public int getRowNum() { return rowNum; }
    public int getColNum() { return colNum; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }
    public void setScreeningId(int screeningId) { this.screeningId = screeningId; }
    public void setRowNum(int rowNum) { this.rowNum = rowNum; }
    public void setColNum(int colNum) { this.colNum = colNum; }
    
}
