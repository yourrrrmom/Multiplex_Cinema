package multiplex.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.ScreeningDisplay;
import multiplex.util.DBconnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CashierController {
    // Αυτά τα fx:id συνδέονται αυτόματα με τα αντίστοιχα στοιχεία του FXML
    @FXML private TextField searchField;
    @FXML private TableView<ScreeningDisplay> tableView;
    @FXML private TableColumn<ScreeningDisplay, String> titleColumn;
    @FXML private TableColumn<ScreeningDisplay, String> timeColumn;
    @FXML private TableColumn<ScreeningDisplay, String> roomColumn;
    @FXML private TableColumn<ScreeningDisplay, Integer> seatsColumn;
    @FXML private Button btnProceed;
    // Η βασική λίστα που περιέχει ΟΛΕΣ τις προβολές
    private final ObservableList<ScreeningDisplay> screeningList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Σύνδεση των στηλών με τα πεδία της κλάσης Screening
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("room"));
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        
        // 2. Γέμισμα της λίστας με δοκιμαστικά δεδομένα
        loadScreeningsFromDatabase();
     
        // 3. Απενεργοποίηση του κουμπιού "Συνέχεια" αρχικά
        btnProceed.setDisable(true);
        
        // 4. Ενεργοποίηση της λογικής του Live Search
        setupLiveSearch();
        
        // 5. Listener για την επιλογή γραμμής στον πίνακα
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> 
            btnProceed.setDisable(newSel == null || newSel.getAvailableSeats() <= 0)
        );
        
        // Refresh data when the window gains focus
        tableView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsW, oldWin, newWin) -> {
                    if (newWin != null) {
                        newWin.focusedProperty().addListener((obsF, wasFocused, isFocused) -> {
                            if (isFocused) {
                                loadScreeningsFromDatabase();
                            }
                        });
                    }
                });
            }
        });
    }

    private void setupLiveSearch() {
        // Τυλίγουμε τα δεδομένα σε μια FilteredList
        FilteredList<ScreeningDisplay> filteredData = new FilteredList<>(screeningList, p -> true);
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
        SortedList<ScreeningDisplay> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        // Βάζουμε την τελική λίστα στον πίνακα
        tableView.setItems(sortedData);
    }

    public void loadScreeningsFromDatabase() {
        screeningList.clear();
        String sql = "SELECT s.Screening_ID, m.Title, s.Start_Time, h.Name AS Room_Name, " +
             "((h.Total_Rows * h.Total_Cols) - COUNT(rs.Screening_ID)) AS Available_Seats, " +
             "s.Ticket_Price " +
             "FROM Screenings s " +
             "JOIN Movies m ON s.Movie_ID = m.Movie_ID " +
             "JOIN Halls h ON s.Hall_ID = h.Hall_ID " +
             "LEFT JOIN Reserved_Seats rs ON s.Screening_ID = rs.Screening_ID " +
             "GROUP BY s.Screening_ID";

        try (Connection conn = DBconnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            //for refresh
            List<ScreeningDisplay> newList = new ArrayList<>();
            while (rs.next()) {
                newList.add(new ScreeningDisplay(
                    rs.getInt("Screening_ID"),
                    rs.getString("Title"),
                    rs.getString("Start_Time"),
                    rs.getString("Room_Name"),
                    rs.getInt("Available_Seats"),
                    rs.getDouble("Ticket_Price")
                ));
            }
            screeningList.setAll(newList); // This refreshes the UI binding correctly
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Αποτυχία: " + e.getMessage());
        }
    }

    @FXML
    private void handleProceed() {
        ScreeningDisplay selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Εδώ θα φορτώνεται η οθόνη με το Πλάνο Θέσεων
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/multiplex_cinema/seating_chart.fxml"));
            Parent root = loader.load();
            SeatingController controller = loader.getController();
            controller.initData(selected); 

            Stage dashboardStage = (Stage) tableView.getScene().getWindow();
            controller.setDashboardStage(dashboardStage);
            controller.setDashboardController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Πλάνο Θέσεων: " + selected.getTitle());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleDeleteReservation() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Διαγραφή Κράτησης");
        dialog.setHeaderText("Εισάγετε το Reservation_ID:");

        dialog.showAndWait().ifPresent(id -> {
            try (Connection conn = DBconnection.getConnection()) {
                // Delete seats first (Foreign Key constraint)
                PreparedStatement ps1 = conn.prepareStatement("DELETE FROM Reserved_Seats WHERE Reservation_ID = ?");
                ps1.setString(1, id);
                ps1.executeUpdate();

                // Delete reservation
                PreparedStatement ps2 = conn.prepareStatement("DELETE FROM Reservations WHERE Reservation_ID = ?");
                ps2.setString(1, id);
                int rows = ps2.executeUpdate();

                if (rows > 0) {
                    loadScreeningsFromDatabase(); // Refresh table
                    showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Η κράτηση διαγράφηκε.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Δεν βρέθηκε κράτηση με αυτό το ID.");
                }
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleLogout() {
        // Logic to close the current window
        Stage currentStage = (Stage) searchField.getScene().getWindow();
        currentStage.close();

        // Logic to reopen login screen
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/mycompany/multiplex_cinema/login.fxml"));
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}