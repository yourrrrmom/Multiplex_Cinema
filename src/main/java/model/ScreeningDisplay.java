package model;

public class ScreeningDisplay {
    private int screeningId;
    private String title;
    private String time;
    private String room;
    private int availableSeats;
    private double price;

    public ScreeningDisplay(int id, String title, String time, String room, int availableSeats, double price) {
        this.screeningId = id;
        this.title = title;
        this.time = time;
        this.room = room;
        this.availableSeats = availableSeats;
        this.price = price;
    }

    public int getScreeningId() {return screeningId;}
    public String getTitle() { return title; }
    public String getTime() { return time; }
    public String getRoom() { return room; }
    public int getAvailableSeats() { return availableSeats; }
    public double getPrice() { return this.price;}
}

