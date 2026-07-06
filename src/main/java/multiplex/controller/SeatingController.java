package multiplex.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import multiplex.util.DBconnection;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import model.ScreeningDisplay;

public class SeatingController {
    private CashierController dashboardController;
    private Stage dashboardStage;
    @FXML private GridPane seatGrid;
    private ScreeningDisplay currentScreening;
    private List<String> selectedSeats = new ArrayList<>(); // Track chosen seats

    public void initData(ScreeningDisplay selected) {
        this.currentScreening = selected;
        seatGrid.getChildren().clear();

        try (Connection conn = DBconnection.getConnection()) {
            // 1. Get Hall Dimensions
            String hallSql = "SELECT Total_Rows, Total_Cols FROM Halls WHERE Name = ?";
            PreparedStatement psHall = conn.prepareStatement(hallSql);
            psHall.setString(1, selected.getRoom());
            ResultSet rsHall = psHall.executeQuery();

            int rows = 0, cols = 0;
            if (rsHall.next()) {
                rows = rsHall.getInt("Total_Rows");
                cols = rsHall.getInt("Total_Cols");
            }

            // 2. Get Taken Seats for this specific screening
            String resSql = "SELECT Row_num, Col_num FROM Reserved_Seats WHERE Screening_ID = ?";
            PreparedStatement psRes = conn.prepareStatement(resSql);
            psRes.setInt(1, selected.getScreeningId());
            ResultSet rsRes = psRes.executeQuery();
            
            List<String> takenSeats = new ArrayList<>();
            while (rsRes.next()) {
                takenSeats.add(rsRes.getInt("Row_num") + "-" + rsRes.getInt("Col_num"));
            }

            // 3. Build the Grid
            for (int r = 1; r <= rows; r++) {
                for (int c = 1; c <= cols; c++) {
                    String seatId = r + "-" + c;
                    Button seat = new Button(seatId);
                    
                    if (takenSeats.contains(seatId)) {
                        seat.setStyle("-fx-background-color: gray;"); // Taken
                        seat.setDisable(true);
                    } else {
                        seat.setStyle("-fx-background-color: green;"); // Available
                        seat.setOnAction(e -> toggleSeatSelection(seat, seatId));
                    }
                    seatGrid.add(seat, c, r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void toggleSeatSelection(Button seat, String seatId) {
        if (selectedSeats.contains(seatId)) {
            selectedSeats.remove(seatId);
            seat.setStyle("-fx-background-color: green;");
        } else {
            selectedSeats.add(seatId);
            seat.setStyle("-fx-background-color: yellow;"); // Highlighted selection
        }
    }
    
    public void setDashboardController(CashierController c) { this.dashboardController = c; }
    public void setDashboardStage(Stage stage) { this.dashboardStage = stage;}
    
    @FXML
    private void handleConfirm() {
        
        // Έλεγχος αν η λίστα είναι άδεια
        if (selectedSeats.isEmpty()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Προσοχή");
            alert.setHeaderText(null);
            alert.setContentText("Παρακαλώ επιλέξτε τουλάχιστον μία θέση για να προχωρήσετε.");
            alert.showAndWait();
            return; // σταματάει την εκτέλεση, δεν ανοίγει το επόμενο παράθυρο
        }
        
        try {
            String fxmlFile = "/com/mycompany/multiplex_cinema/reservation_complete.fxml";
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlFile));
            javafx.scene.Parent root = loader.load();

            ReservationController controller = loader.getController();
            controller.setDashboardController(this.dashboardController);
            controller.setDashboardStage(this.dashboardStage);

            // Παίρνουμε το πλήθος των θέσεων και το πολλαπλασιάζουμε με την τιμή της προβολής
            double total = selectedSeats.size() * currentScreening.getTicketPrice();
        
            // Στέλνουμε τα δεδομένα (και το νέο total) στον ReservationController
            controller.initReservation(currentScreening, selectedSeats, total);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Ολοκλήρωση Κράτησης");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBack() {
        Stage stage = (Stage) seatGrid.getScene().getWindow();
        stage.close();
    }
}