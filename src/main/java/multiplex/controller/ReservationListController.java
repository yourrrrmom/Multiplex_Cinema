package multiplex.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.sql.*;
import model.ReservationDisplay;
import multiplex.util.DBconnection;

public class ReservationListController {
    private CashierController parentController;
    
    @FXML private TableView<ReservationDisplay> reservationsTable;
    @FXML private TableColumn<ReservationDisplay, Integer> resIdColumn;
    @FXML private TableColumn<ReservationDisplay, String> customerColumn;
    @FXML private TableColumn<ReservationDisplay, String> movieColumn;
    @FXML private TableColumn<ReservationDisplay, Double> priceColumn;
    @FXML private Button btnDelete;

    @FXML
    public void initialize() {
        resIdColumn.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        movieColumn.setCellValueFactory(new PropertyValueFactory<>("movieDetails"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        loadReservations();

        // Ενεργοποίηση του κουμπιού διαγραφής μόνο όταν επιλεγεί μια γραμμή
        reservationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            btnDelete.setDisable(newSel == null);
        });
    }

    private void loadReservations() {
       // Καθαρισμός του πίνακα πριν το γέμισμα
        reservationsTable.getItems().clear();
        
        ObservableList<ReservationDisplay> list = FXCollections.observableArrayList();
        
        // Το SQL query για να πάρεις τα δεδομένα από τους συνδεδεμένους πίνακες
        String sql = "SELECT r.Reservation_ID, m.Title, s.Start_Time, r.Total_Price " +
                     "FROM Reservations r " +
                     "JOIN Screenings s ON r.Screening_ID = s.Screening_ID " +
                     "JOIN Movies m ON s.Movie_ID = m.Movie_ID";

        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Συνδυασμός Ταινίας και Ώρας για εμφάνιση σε μία στήλη
                String movieDetails = rs.getString("Title") + " (" + rs.getString("Start_Time") + ")";
                
                list.add(new ReservationDisplay(
                    rs.getInt("Reservation_ID"),
                    movieDetails,
                    rs.getDouble("Total_Price")
                ));
            }
            
            // Τοποθέτηση των δεδομένων στον πίνακα
            reservationsTable.setItems(list);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setParentController(CashierController controller) { this.parentController = controller;}
    
    @FXML
    public void handleDelete() {
        ReservationDisplay selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try (Connection conn = DBconnection.getConnection()) {
            // 1. Σβήσε πρώτα τις θέσεις που αντιστοιχούν σε αυτή την κράτηση
            PreparedStatement ps1 = conn.prepareStatement("DELETE FROM Reserved_Seats WHERE Reservation_ID = ?");
            ps1.setInt(1, selected.getReservationId());
            ps1.executeUpdate();

            // 2. Σβήσε την κράτηση
            PreparedStatement ps2 = conn.prepareStatement("DELETE FROM Reservations WHERE Reservation_ID = ?");
            ps2.setInt(1, selected.getReservationId());
            ps2.executeUpdate();

            loadReservations();

            // 3. Ενημέρωσε τον Cashier για να ξαναμετρήσει τις θέσεις
            if (parentController != null) {
                parentController.loadScreeningsFromDatabase();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) btnDelete.getScene().getWindow();
        stage.close();
    }
}
