package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchController {

    // Αυτά τα fx:id συνδέονται αυτόματα με τα αντίστοιχα στοιχεία του FXML
    @FXML private TextField searchField;
    @FXML private TableView<Screening> tableView;
    @FXML private TableColumn<Screening, String> titleColumn;
    @FXML private TableColumn<Screening, String> timeColumn;
    @FXML private TableColumn<Screening, String> roomColumn;
    @FXML private TableColumn<Screening, Integer> seatsColumn;
    @FXML private Button btnProceed;

    // Η βασική λίστα που περιέχει ΟΛΕΣ τις προβολές
    private final ObservableList<Screening> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Σύνδεση των στηλών με τα πεδία της κλάσης Screening
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("room"));
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));

        // 2. Γέμισμα της λίστας με δοκιμαστικά δεδομένα
        loadDataFromDatabase();

        // 3. Απενεργοποίηση του κουμπιού "Συνέχεια" αρχικά
        btnProceed.setDisable(true);

        // 4. Ενεργοποίηση της λογικής του Live Search
        setupLiveSearch();

        // 5. Listener για την επιλογή γραμμής στον πίνακα
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getAvailableSeats() > 0) {
                btnProceed.setDisable(false); // Ενεργοποίηση αν υπάρχουν θέσεις
            } else {
                btnProceed.setDisable(true);  // Sold out ή καμία επιλογή
            }
        });
    }

    private void setupLiveSearch() {
        // Τυλίγουμε τα δεδομένα σε μια FilteredList
        FilteredList<Screening> filteredData = new FilteredList<>(masterData, p -> true);

        // Προσθέτουμε Listener στο text field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(screening -> {
                // Αν το πεδίο είναι άδειο, δείξε όλες τις ταινίες
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Μετατροπή σε πεζά για ευκολία στην αναζήτηση
                String lowerCaseFilter = newValue.toLowerCase();

                // Αναζήτηση με βάση τον τίτλο
                if (screening.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                
                return false; // Δεν ταιριάζει
            });
        });

        // Τυλίγουμε την FilteredList σε SortedList για να δουλεύει η ταξινόμηση στηλών
        SortedList<Screening> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        // Βάζουμε την τελική λίστα στον πίνακα
        tableView.setItems(sortedData);
    }

    private void loadDataFromDatabase() {
        // Το URL της βάσης σας (προσαρμόστε το όνομα του αρχείου αν είναι διαφορετικό)
        String url = "jdbc:sqlite:multiplex.db"; 
    
        // Το "έξυπνο" SQL ερώτημα που κάνει όλο τον υπολογισμό μόνο του
        String query = 
            "SELECT " +
            "   m.Title, " +
            "   s.Start_Time, " +
            "   h.Name AS Room_Name, " +
            "   ((h.Total_Rows * h.Total_Cols) - COUNT(rs.Screening_ID)) AS Available_Seats " +
            "FROM Screenings s " +
            "JOIN Movies m ON s.Movie_ID = m.Movie_ID " +
            "JOIN Halls h ON s.Hall_ID = h.Hall_ID " +
            "LEFT JOIN Reserved_Seats rs ON s.Screening_ID = rs.Screening_ID " +
            "GROUP BY s.Screening_ID;";

        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            // Καθαρίζουμε τη λίστα (χρήσιμο αν π.χ. ο ταμίας πατήσει "Ανανέωση")
            masterData.clear();

            while (rs.next()) {
                String dbTitle = rs.getString("Title");
                String dbTime = rs.getString("Start_Time");
                String dbRoom = rs.getString("Room_Name");
                int dbSeats = rs.getInt("Available_Seats");

                // Δημιουργούμε το αντικείμενο Screening και το βάζουμε στη λίστα του πίνακα
                masterData.add(new Screening(dbTitle, dbTime, dbRoom, dbSeats));
            }
        
        } catch (SQLException e) {
            System.out.println("Σφάλμα κατά τη φόρτωση από τη βάση: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleProceed() {
        Screening selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Επιτυχία! Προχωράμε στο πλάνο θέσεων για την ταινία : " + selected.getTitle());
            // Εδώ θα φορτώνεται η οθόνη με το Πλάνο Θέσεων
        }
    }
}