package multiplex.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import multiplex.util.DBconnection;
import java.sql.*;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.stage.StageStyle;
import model.ScreeningDisplay;

public class ReservationController {
    private CashierController dashboardController;
    private Stage dashboardStage;
    @FXML private Label movieTitleLabel;
    @FXML private Label detailsLabel;
    @FXML private Label totalLabel;

    private ScreeningDisplay screening;
    private List<String> seats;
    private double total;
    
    // This data is passed from the SeatingController
    public void initReservation(ScreeningDisplay screening, List<String> seats, double total) {
        this.screening = screening;
        this.seats = seats;
        this.total = total;
        movieTitleLabel.setText(screening.getTitle());
        detailsLabel.setText("Αίθουσα: " + screening.getRoom() + " | Ώρα: " + screening.getTime());
        totalLabel.setText("Σύνολο: " + String.format("%.2f", total) + " €");
    }

    @FXML
    void handleFinalizePayment() {
        Connection conn = null;
        try {
            conn = DBconnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert into Reservations
            String resSql = "INSERT INTO Reservations (Screening_ID, Created_By_User_ID, Customer_Name, Total_Price, Payment_Status) VALUES (?, 2, 'Customer', ?, 'paid')";
            PreparedStatement psRes = conn.prepareStatement(resSql, Statement.RETURN_GENERATED_KEYS);
            psRes.setInt(1, screening.getScreeningId());
            psRes.setDouble(2, total);
            psRes.executeUpdate();

            ResultSet rs = psRes.getGeneratedKeys();
            int resId = rs.next() ? rs.getInt(1) : -1;

            // 2. Insert Reserved Seats
            String seatSql = "INSERT INTO Reserved_Seats (Reservation_ID, Screening_ID, Row_num, Col_num) VALUES (?, ?, ?, ?)";
            PreparedStatement psSeat = conn.prepareStatement(seatSql);
            for (String seatId : seats) {
                String[] parts = seatId.split("-");
                psSeat.setInt(1, resId);
                psSeat.setInt(2, screening.getScreeningId());
                psSeat.setInt(3, Integer.parseInt(parts[0]));
                psSeat.setInt(4, Integer.parseInt(parts[1]));
                psSeat.addBatch();
            }
            psSeat.executeBatch();
            conn.commit(); //conclude Transaction
            showSuccessMessage(resId);
            System.out.println("Κράτηση ολοκληρώθηκε!");
        } catch (Exception e) {
            if(conn != null){
                try { conn.rollback();} catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace(); 
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    public void setDashboardController(CashierController c) { this.dashboardController = c; }
    public void setDashboardStage(Stage stage) { this.dashboardStage = stage;}
    
    private void showSuccessMessage(int reservationId) {
        Stage successStage = new Stage();
        successStage.initStyle(StageStyle.UNDECORATED); // Makes it look like a popup
        VBox box = new VBox(
                new Label("Η πληρωμή ολοκληρώθηκε με επιτυχία!"),
                new Label("Κωδικός Κράτησης: " + reservationId)
        );
        box.setAlignment(javafx.geometry.Pos.CENTER); // Center text
        box.setStyle("-fx-background-color: #d4edda; -fx-padding: 20; -fx-border-color: #28a745;");

        successStage.setScene(new Scene(box));
        successStage.show();

        // Disappear after 10 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            if (this.dashboardController != null) {
                this.dashboardController.loadScreeningsFromDatabase();
            }
            successStage.close();
            
            if (this.dashboardStage != null) {
                Object controller = this.dashboardStage.getScene().getRoot().getProperties().get("controller");
                if (this.dashboardStage.getUserData() instanceof CashierController) {
                    ((CashierController) this.dashboardStage.getUserData()).loadScreeningsFromDatabase();
                }
            }
            // Get the window of the current reservation screen
            Stage currentReservationStage = (Stage) movieTitleLabel.getScene().getWindow();

            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    // Close if it's not the dashboard AND not the success popup
                    if (stage != this.dashboardStage && stage != successStage) {
                        stage.close();
                    }
                }
            }
            // Explicitly ensure the current window is closed
            if (currentReservationStage != null) {
                currentReservationStage.close();
            }
        });
        delay.play();
    }
    
    @FXML
    private void handleBack() {
        Stage stage = (Stage) movieTitleLabel.getScene().getWindow();
        stage.close();
    }
}