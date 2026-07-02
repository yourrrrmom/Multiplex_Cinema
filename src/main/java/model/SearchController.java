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
    String url = "jdbc:sqlite:multiplex.db"; 

    // Ενώνουμε τον πίνακα Screenings με τον πίνακα Movies μέσω του Movie_ID
    String query = "SELECT m.Title, s.Start_Time, s.Hall_ID " +
                   "FROM Screenings s " +
                   "JOIN Movies m ON s.Movie_ID = m.Movie_ID"; 

    try (Connection conn = DriverManager.getConnection(url);
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {

        // Καθαρίζουμε τη λίστα πριν τη γεμίσουμε (καλή πρακτική)
        masterData.clear();

        while (rs.next()) {
            // 1. Παίρνουμε τον Τίτλο από τον πίνακα Movies
            String dbTitle = rs.getString("Title");

            // 2. Παίρνουμε την Ώρα Έναρξης από τον πίνακα Screenings
            String dbTime = rs.getString("Start_Time");

            // 3. Παίρνουμε το Hall_ID και το μετατρέπουμε σε κείμενο "Αίθουσα Χ"
            String dbRoom = "Αίθουσα " + rs.getInt("Hall_ID");

            // 4. Επειδή δεν υπάρχει στήλη θέσεων ακόμα, βάζουμε π.χ. 60 θέσεις hardcoded
            int dbSeats = 60; 

            // Τα "ταΐζουμε" στην κλάση Screening της κοπέλας για να εμφανιστούν στον πίνακα!
            masterData.add(new Screening(dbTitle, dbTime, dbRoom, dbSeats));
        }

    } catch (SQLException e) {
        System.out.println("Σφάλμα κατά τη φόρτωση από τη βάση: " + e.getMessage());
        e.printStackTrace(); // Αυτό θα βοηθήσει αν πετάξει κόκκινο σφάλμα στην κονσόλα
    }
}

    
    @FXML
    private void handleProceed() {
        Screening selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Επιτυχία! Προχωράμε στο πλάνο θέσεων για την ταινία: " + selected.getTitle());
            // Εδώ, αργότερα, θα φορτώνεις την οθόνη του συναδέλφου σου (Πλάνο Θέσεων)
        }
    }
}
