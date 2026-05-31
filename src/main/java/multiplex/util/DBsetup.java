package multiplex.util;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DBsetup {
    public static void main(String[] args) {
        // ο κώδικας SQL για τους πίνακες και τα δεδομένα (συμβατό με Java 11)
        String createTablesAndInsertData = 
            "-- Πίνακας Χρηστών (Ταμίες & Διαχειριστές)\n" +
            "CREATE TABLE IF NOT EXISTS Users (\n" +
            "    User_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    Username TEXT UNIQUE NOT NULL,\n" +
            "    Password TEXT NOT NULL,\n" +
            "    Role TEXT NOT NULL CHECK(Role IN ('manager', 'cashier'))\n" +
            ");\n" +
            "\n" +
            "-- Πίνακας Ταινιών\n" +
            "CREATE TABLE IF NOT EXISTS Movies (\n" +
            "    Movie_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    Title TEXT NOT NULL,\n" +
            "    Description TEXT,\n" +
            "    Duration_mins INTEGER NOT NULL,\n" +
            "    Genre TEXT,\n" +
            "    Poster_path TEXT\n" +
            ");\n" +
            "\n" +
            "-- Πίνακας Αιθουσών\n" +
            "CREATE TABLE IF NOT EXISTS Halls (\n" +
            "    Hall_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    Name TEXT NOT NULL,\n" +
            "    Total_Rows INTEGER NOT NULL,\n" +
            "    Total_Cols INTEGER NOT NULL\n" +
            ");\n" +
            "\n" +
            "-- Πίνακας Προβολών\n" +
            "CREATE TABLE IF NOT EXISTS Screenings (\n" +
            "    Screening_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    Movie_ID INTEGER NOT NULL,\n" +
            "    Hall_ID INTEGER NOT NULL,\n" +
            "    Start_Time DATETIME NOT NULL,\n" +
            "    Ticket_Price DECIMAL(5,2) NOT NULL,\n" +
            "    FOREIGN KEY (Movie_ID) REFERENCES Movies(Movie_ID) ON DELETE CASCADE,\n" +
            "    FOREIGN KEY (Hall_ID) REFERENCES Halls(Hall_ID) ON DELETE CASCADE\n" +
            ");\n" +
            "\n" +
            "-- Πίνακας Κρατήσεων\n" +
            "CREATE TABLE IF NOT EXISTS Reservations (\n" +
            "    Reservation_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    Screening_ID INTEGER NOT NULL,\n" +
            "    Created_By_User_ID INTEGER NOT NULL,\n" +
            "    Customer_Name TEXT NOT NULL,\n" +
            "    Total_Price DECIMAL(7,2) NOT NULL,\n" +
            "    Payment_Status TEXT NOT NULL CHECK(Payment_Status IN ('paid', 'refunded')),\n" +
            "    Created_at DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
            "    FOREIGN KEY (Screening_ID) REFERENCES Screenings(Screening_ID),\n" +
            "    FOREIGN KEY (Created_By_User_ID) REFERENCES Users(User_ID)\n" +
            ");\n" +
            "\n" +
            "-- Πίνακας Δεσμευμένων Θέσεων\n" +
            "CREATE TABLE IF NOT EXISTS Reserved_Seats (\n" +
            "    Reservation_ID INTEGER NOT NULL,\n" +
            "    Screening_ID INTEGER NOT NULL,\n" +
            "    Row_num INTEGER NOT NULL,\n" +
            "    Col_num INTEGER NOT NULL,\n" +
            "    PRIMARY KEY (Screening_ID, Row_num, Col_num),\n" +
            "    FOREIGN KEY (Reservation_ID) REFERENCES Reservations(Reservation_ID) ON DELETE CASCADE,\n" +
            "    FOREIGN KEY (Screening_ID) REFERENCES Screenings(Screening_ID) ON DELETE CASCADE\n" +
            ");\n" +
            "\n" +
            "-- --- ΕΙΣΑΓΩΓΗ ΕΙΚΟΝΙΚΩΝ ΔΕΔΟΜΕΝΩΝ (MOCK DATA) ---\n" +
            "\n" +
            "-- Χρήστες\n" +
            "INSERT OR IGNORE INTO Users (User_ID, Username, Password, Role) VALUES (1, 'admin', 'admin123', 'manager');\n" +
            "INSERT OR IGNORE INTO Users (User_ID, Username, Password, Role) VALUES (2, 'cashier1', 'cashier123', 'cashier');\n" +
            "\n" +
            "-- Ταινίες\n" +
            "INSERT OR IGNORE INTO Movies (Movie_ID, Title, Description, Duration_mins, Genre, Poster_path) \n" +
            "VALUES (1, 'Inception', 'A thief who steals corporate secrets through the use of dream-sharing technology.', 148, 'Sci-Fi', '/images/inception.png');\n" +
            "\n" +
            "INSERT OR IGNORE INTO Movies (Movie_ID, Title, Description, Duration_mins, Genre, Poster_path) \n" +
            "VALUES (2, 'The Dark Knight', 'When the menace known as the Joker wreaks havoc on Gotham..', 152, 'Action', '/images/dark_knight.png');\n" +
            "\n" +
            "-- Αίθουσες\n" +
            "INSERT OR IGNORE INTO Halls (Hall_ID, Name, Total_Rows, Total_Cols) VALUES (1, 'Αίθουσα 1 IMAX', 5, 6);\n" +
            "INSERT OR IGNORE INTO Halls (Hall_ID, Name, Total_Rows, Total_Cols) VALUES (2, 'Αίθουσα 2 VIP', 4, 4);\n" +
            "\n" +
            "-- Προβολές\n" +
            "INSERT OR IGNORE INTO Screenings (Screening_ID, Movie_ID, Hall_ID, Start_Time, Ticket_Price) \n" +
            "VALUES (1, 1, 1, '2026-06-15 19:30:00', 8.50);\n" +
            "\n" +
            "INSERT OR IGNORE INTO Screenings (Screening_ID, Movie_ID, Hall_ID, Start_Time, Ticket_Price) \n" +
            "VALUES (2, 2, 2, '2026-06-15 22:30:00', 9.00);\n" +
            "\n" +
            "-- Κρατήσεις\n" +
            "-- Μια έτοιμη πληρωμένη κράτηση για τον Γιώργο με 2 εισιτήρια\n" +
            "INSERT OR IGNORE INTO Reservations (Reservation_ID, Screening_ID, Created_By_User_ID, Customer_Name, Total_Price, Payment_Status) \n" +
            "VALUES (1, 1, 2, 'Γιώργος Πρεζεράκος', 17.00, 'paid');\n" +
            "\n" +
            "-- Δεσμευμένες θέσεις\n" +
            "-- Οι 2 θέσεις του Γιώργου (Σειρά 3, Στήλες 4 και 5)\n" +
            "INSERT OR IGNORE INTO Reserved_Seats (Reservation_ID, Screening_ID, Row_num, Col_num) VALUES (1, 1, 3, 4);\n" +
            "INSERT OR IGNORE INTO Reserved_Seats (Reservation_ID, Screening_ID, Row_num, Col_num) VALUES (1, 1, 3, 5);\n";

        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(createTablesAndInsertData); 
            System.out.println("Επιτυχία! Η βάση δεδομένων είναι 100% έτοιμη και συγχρονισμένη με το SRS!");

        } catch (SQLException e) {
            System.out.println("Πρόβλημα κατά τη δημιουργία της βάσης: " + e.getMessage());
        }
    }
}