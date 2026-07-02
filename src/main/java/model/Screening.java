package model;
public class Screening {
    private String title;
    private String time;
    private String room;
    private int availableSeats;

    public Screening(String title, String time, String room, int availableSeats) {
        this.title = title;
        this.time = time;
        this.room = room;
        this.availableSeats = availableSeats;
    }

    public String getTitle() { return title; }
    public String getTime() { return time; }
    public String getRoom() { return room; }
    public int getAvailableSeats() { return availableSeats; }
}

