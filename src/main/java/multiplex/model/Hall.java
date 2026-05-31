
package multiplex.model;

public class Hall {
    private int hallId;
    private String name;
    private int totalRows;
    private int totalCols;

    public Hall(int hallId, String name, int totalRows, int totalCols) {
        this.hallId = hallId;
        this.name = name;
        this.totalRows = totalRows;
        this.totalCols = totalCols;
    }

    // Getters & Setters
    public int getHallId() { return hallId; }
    public String getName() { return name; }
    public int getTotalRows() { return totalRows; }
    public int getTotalCols() { return totalCols; }
    public void setHallId(int hallId) { this.hallId = hallId; }
    public void setName(String name) { this.name = name; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public void setTotalCols(int totalCols) { this.totalCols = totalCols; }
    
}
