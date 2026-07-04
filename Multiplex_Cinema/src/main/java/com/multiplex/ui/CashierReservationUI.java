package com.multiplex.ui;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import static javafx.application.Application.launch;

public class CashierReservationUI extends Application {    

    private static final double TICKET_PRICE = 8.50;
    private double totalAmount = 0.0;
    private List<Button> selectedSeats = new ArrayList<>();
    private Label totalLabel;

    private static final String COLOR_AVAILABLE = "-fx-background-color: #4EC498; -fx-text-fill: white; -fx-font-weight: bold;"; // GREEN
    private static final String COLOR_OCCUPIED = "-fx-background-color: #808286; -fx-text-fill: white; -fx-font-weight: bold;";  // GRAY
    private static final String COLOR_SELECTED = "-fx-background-color: #B66C8E; -fx-text-fill: black; -fx-font-weight: bold;";  // RED

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // --- Header ---
        Label titleLabel = new Label("Πλάνο Αίθουσας");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        VBox header = new VBox(5, titleLabel);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10, 100, 10, 100));
        
        HBox headerWrapper = new HBox(header);
        headerWrapper.setAlignment(Pos.CENTER);
        headerWrapper.setPadding(new Insets(0, 0, 20, 0));
        root.setTop(headerWrapper);
       
        // 1. Create the Seat Grids
        GridPane leftBlock = createSeatGrid(12, 5);
        GridPane centerBlock = createSeatGrid(16, 12);
        GridPane rightBlock = createSeatGrid(12, 5);

        // 2. Create the Entries (We give them the same width as a block or specific width)
        Label leftEntry = new Label("Είσοδος");
        leftEntry.setAlignment(Pos.CENTER);
        leftEntry.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-padding: 5 15;");
        leftEntry.setMinWidth(120); 

        Label rightEntry = new Label("Είσοδος");
        rightEntry.setAlignment(Pos.CENTER);
        rightEntry.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-padding: 5 15;");
        rightEntry.setMinWidth(120);

        // 3. Create the Entry Row
        HBox entryRow = new HBox(40); 
        entryRow.setAlignment(Pos.TOP_CENTER); // Pin to the top
        // Create a spacer that acts as the "anchor" for the center block
        Region centerAisleSpacer = new Region();
        centerAisleSpacer.setPrefWidth(200); // Width of your center block
        // Force the labels to stay their specific size and not stretch
        leftEntry.setMaxWidth(90);
        leftEntry.setMinWidth(90);
        rightEntry.setMaxWidth(90);
        rightEntry.setMinWidth(90);
        entryRow.getChildren().addAll(leftEntry, centerAisleSpacer, rightEntry);
        
        // 4. Create the Seating Area
        HBox seatingArea = new HBox(0);
        seatingArea.setAlignment(Pos.BOTTOM_CENTER);
        seatingArea.setFillHeight(false);
        seatingArea.setMaxWidth(700);
        
        Region aisle1 = new Region();
        aisle1.setPrefWidth(30);
        
        Region aisle2 = new Region();
        aisle2.setPrefWidth(30);
        
        seatingArea.getChildren().addAll(
            createBottomAlignedWrapper(leftBlock),
            aisle1,
            createBottomAlignedWrapper(centerBlock),
            aisle2,
            createBottomAlignedWrapper(rightBlock)
        );

        // 5. Combine into a VBox for the final layout
        VBox middleLayout = new VBox(40, entryRow, seatingArea);
        middleLayout.setAlignment(Pos.CENTER);

        // --- 3. Bottom Section: Stage, Screen & Exit ---
        Label exit = new Label("Έξοδος");
        exit.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-padding: 5 20;");
        
        Label screen = new Label("ΟΘΟΝΗ");
        screen.setAlignment(Pos.CENTER);
        screen.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: black; -fx-padding: 5 150;");
        screen.setPrefWidth(500);
        screen.setPadding(new Insets(5, 0, 5, 0));
        
        Label stage = new Label();
        stage.setStyle("-fx-background-color: #E0E0E0;");
        stage.setPrefSize(800, 100);
        
        StackPane stageArea = new StackPane(stage, screen);
        stageArea.setAlignment(Pos.CENTER);
        
        HBox exitContainer = new HBox(exit);
        exitContainer.setAlignment(Pos.BOTTOM_RIGHT);
        exitContainer.setMaxWidth(800);
        exitContainer.setPrefWidth(800);
        exitContainer.setPrefHeight(60);
        
        VBox bottomArea = new VBox(5,exitContainer, stageArea);
        bottomArea.setAlignment(Pos.CENTER);

        // --- Combine Everything ---
        VBox theaterCard = new VBox(20, middleLayout, bottomArea); 
        theaterCard.setStyle("-fx-background-color: #EEEEEE; -fx-background-radius: 10; -fx-padding: 20;");
        theaterCard.setMaxWidth(700); 
        theaterCard.setAlignment(Pos.CENTER);

        // Set the background for the entire window
        root.setStyle("-fx-background-color: #FFFFFF;"); 

        // Place the 'theaterCard' in the center
        root.setCenter(theaterCard);
        BorderPane.setAlignment(theaterCard, Pos.CENTER);
        BorderPane.setMargin(theaterCard, new Insets(0, 0, 20, 0));

        // --- Footer (Πληρωμή) ---
        // Keep this separate so it stays at the very bottom of the window
        totalLabel = new Label("Σύνολο: 0.00 €");
        totalLabel.setFont(new Font("Arial", 18));

        Button payButton = new Button("Ολοκλήρωση Πληρωμής");
        payButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        payButton.setOnAction(e -> processPayment());

        HBox footer = new HBox(20, totalLabel, payButton);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(20)); // Added padding for better look
        root.setBottom(footer);

        Scene scene = new Scene(root, 800, 800);
        primaryStage.setTitle("Multiplex Cinema - Σύστημα Κρατήσεων");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private static final String BUTTON_BASE_STYLE = "-fx-padding: 0; -fx-background-radius: 2; ";
    
    // Helper method to generate distinct seating blocks
    private GridPane createSeatGrid(int rows, int cols) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(3);
        grid.setHgap(3);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Button seat = new Button();

                // FIX 2: Corrected MaxSize (16, 16)
                seat.setPrefSize(16, 16);
                seat.setMinSize(16, 16);
                seat.setMaxSize(16, 16);

                if (Math.random() < 0.15) {
                    seat.setStyle(BUTTON_BASE_STYLE + COLOR_OCCUPIED);
                    seat.setDisable(true);
                } else {
                    seat.setStyle(BUTTON_BASE_STYLE + COLOR_AVAILABLE);
                    seat.setOnAction(e -> handleSeatSelection(seat));
                }
                grid.add(seat, col, row);
            }
        }
        return grid;
    }
    
    private VBox createBottomAlignedWrapper(GridPane grid) {
        grid.setPrefHeight(304); 
        grid.setMinHeight(304);
        grid.setAlignment(Pos.BOTTOM_CENTER);
        VBox wrapper = new VBox(grid);
        wrapper.setAlignment(Pos.BOTTOM_CENTER); // Pins the grid to the bottom
        wrapper.setPrefHeight(304);
        wrapper.setFillWidth(false);
        return wrapper;
    }

    // Διαχείριση κλικ σε θέση
    private void handleSeatSelection(Button seat) {
        if (selectedSeats.contains(seat)) {
            // Αποεπιλογή
            seat.setStyle(BUTTON_BASE_STYLE + COLOR_AVAILABLE);
            selectedSeats.remove(seat);
            totalAmount -= TICKET_PRICE;
        } else {
            seat.setStyle(BUTTON_BASE_STYLE + COLOR_SELECTED);
            selectedSeats.add(seat);
            totalAmount += TICKET_PRICE;
        }
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        totalLabel.setText(String.format("Σύνολο: %.2f €", totalAmount));
    }

    // Προσομοίωση αποστολής αιτήματος στο Τραπεζικό Σύστημα
    private void processPayment() {
        if (selectedSeats.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Προσοχή", "Δεν έχετε επιλέξει θέσεις!");
            return;
        }

        // Εδώ κανονικά γίνεται η κλήση στο Banking System API [cite: 45]
        boolean bankApproval = simulateBankAPIRequest();

        if (bankApproval) {
            // Επιτυχής Πληρωμή: Δέσμευση θέσεων
            for (Button seat : selectedSeats) {
                seat.setStyle(COLOR_OCCUPIED);
                seat.setDisable(true);
            }
            selectedSeats.clear();
            totalAmount = 0.0;
            updateTotalLabel();
            
            showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Η πληρωμή εγκρίθηκε και τα εισιτήρια εκδόθηκαν επιτυχώς!");
        } else {
            // Απόρριψη Πληρωμής
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Η συναλλαγή απορρίφθηκε από το Τραπεζικό Σύστημα. Παρακαλώ δοκιμάστε ξανά.");
        }
    }

    // Mock μέθοδος για την έγκριση της τράπεζας (επιστρέφει True/False) 
    private boolean simulateBankAPIRequest() {
        // Προσομοίωση 90% πιθανότητας επιτυχίας
        return Math.random() > 0.1; 
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}