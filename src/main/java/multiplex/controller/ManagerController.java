package multiplex.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import multiplex.model.Movie;
import multiplex.model.Hall;
import multiplex.util.DBconnection;

public class ManagerController {

    // --- ΠΕΔΙΑ ΤΑΙΝΙΩΝ (FXML) ---
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextField durationField;
    @FXML private TextField genreField;

    @FXML private TableView<Movie> movieTable;
    @FXML private TableColumn<Movie, Integer> colMovieId;
    @FXML private TableColumn<Movie, String> colMovieTitle;
    @FXML private TableColumn<Movie, Integer> colMovieDuration;
    @FXML private TableColumn<Movie, String> colMovieGenre;

    // --- ΠΕΔΙΑ ΑΙΘΟΥΣΩΝ (FXML) ---
    @FXML private TextField hallNameField;
    @FXML private TextField rowsField;
    @FXML private TextField colsField;

    @FXML private TableView<Hall> hallTable;
    @FXML private TableColumn<Hall, Integer> colHallId;
    @FXML private TableColumn<Hall, String> colHallName;
    @FXML private TableColumn<Hall, Integer> colHallRows;
    @FXML private TableColumn<Hall, Integer> colHallCols;

    // --- CONTAINER VBOX ---
    @FXML private VBox movie;
    @FXML private VBox hall;

    private ObservableList<Movie> movieList = FXCollections.observableArrayList();
    private ObservableList<Hall> hallList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // --- MOVIES ---
        colMovieId.setCellValueFactory(new PropertyValueFactory<>("movieId"));
        colMovieTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colMovieDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colMovieGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        movieTable.setItems(movieList);
        loadMoviesFromDatabase();

        // --- HALLS ---
        colHallId.setCellValueFactory(new PropertyValueFactory<>("hallId"));
        colHallName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colHallRows.setCellValueFactory(new PropertyValueFactory<>("totalRows"));
        colHallCols.setCellValueFactory(new PropertyValueFactory<>("totalCols"));
        hallTable.setItems(hallList);
        loadHallsFromDatabase();
    }

    private void loadMoviesFromDatabase() {
        movieList.clear();
        String sql = "SELECT * FROM Movies";
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                movieList.add(new Movie(
                        rs.getInt("Movie_ID"), rs.getString("Title"),
                        rs.getString("Description"), rs.getInt("Duration_mins"),
                        rs.getString("Genre"), rs.getString("Poster_path")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Αποτυχία φόρτωσης ταινιών: " + e.getMessage());
        }
    }

    private void loadHallsFromDatabase() {
        hallList.clear();
        String sql = "SELECT * FROM Halls";
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                hallList.add(new Hall(
                        rs.getInt("Hall_ID"), rs.getString("Name"),
                        rs.getInt("Total_Rows"), rs.getInt("Total_Cols")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Αποτυχία φόρτωσης αιθουσών: " + e.getMessage());
        }
    }

    // =========================================================================
    // --- ΜΕΘΟΔΟΙ ΔΙΑΧΕΙΡΙΣΗΣ ΤΑΙΝΙΩΝ ---
    // =========================================================================

    @FXML
    private void handleMovieTableClick() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            titleField.setText(selectedMovie.getTitle());
            descriptionField.setText(selectedMovie.getDescription());
            durationField.setText(String.valueOf(selectedMovie.getDuration()));
            genreField.setText(selectedMovie.getGenre());
        }
    }

    @FXML
    private void handleAddMovie() {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String durationStr = durationField.getText().trim();
        String genre = genreField.getText().trim();

        if (title.isEmpty() || durationStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Παρακαλώ συμπληρώστε Τίτλο και Διάρκεια!");
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            String sql = "INSERT INTO Movies (Title, Description, Duration_mins, Genre, Poster_path) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DBconnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title); pstmt.setString(2, description);
                pstmt.setInt(3, duration); pstmt.setString(4, genre); pstmt.setString(5, "");
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Η ταινία προστέθηκε!");
                clearMovieFields();
                loadMoviesFromDatabase();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Ελέγξτε τα δεδομένα: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateMovie() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            showAlert(Alert.AlertType.WARNING, "Προσοχή", "Επιλέξτε πρώτα μια ταινία από τον πίνακα!");
            return;
        }

        String sql = "UPDATE Movies SET Title = ?, Description = ?, Duration_mins = ?, Genre = ? WHERE Movie_ID = ?";
        try (Connection conn = DBconnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, titleField.getText().trim());
            pstmt.setString(2, descriptionField.getText().trim());
            pstmt.setInt(3, Integer.parseInt(durationField.getText().trim()));
            pstmt.setString(4, genreField.getText().trim());
            pstmt.setInt(5, selectedMovie.getMovieId());

            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Η ταινία ενημερώθηκε!");
            clearMovieFields();
            loadMoviesFromDatabase();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Αποτυχία ενημέρωσης: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteMovie() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            showAlert(Alert.AlertType.WARNING, "Προσοχή", "Επιλέξτε πρώτα μια ταινία από τον πίνακα!");
            return;
        }

        String sql = "DELETE FROM Movies WHERE Movie_ID = ?";
        try (Connection conn = DBconnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selectedMovie.getMovieId());
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Η ταινία διαγράφηκε!");
            clearMovieFields();
            loadMoviesFromDatabase();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Αποτυχία διαγραφής: " + e.getMessage());
        }
    }

    private void clearMovieFields() {
        titleField.clear(); descriptionField.clear(); durationField.clear(); genreField.clear();
    }

    // =========================================================================
    // --- ΜΕΘΟΔΟΙ ΔΙΑΧΕΙΡΙΣΗΣ ΑΙΘΟΥΣΩΝ ---
    // =========================================================================

    @FXML
    private void handleHallTableClick() {
        Hall selectedHall = hallTable.getSelectionModel().getSelectedItem();
        if (selectedHall != null) {
            hallNameField.setText(selectedHall.getName());
            rowsField.setText(String.valueOf(selectedHall.getTotalRows()));
            colsField.setText(String.valueOf(selectedHall.getTotalCols()));
        }
    }

    @FXML
    private void handleAddHall() {
        String name = hallNameField.getText().trim();
        String rowsStr = rowsField.getText().trim();
        String colsStr = colsField.getText().trim();

        if (name.isEmpty() || rowsStr.isEmpty() || colsStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Συμπληρώστε όλα τα πεδία!");
            return;
        }

        try {
            String sql = "INSERT INTO Halls (Name, Total_Rows, Total_Cols) VALUES (?, ?, ?)";
            try (Connection conn = DBconnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, Integer.parseInt(rowsStr));
                pstmt.setInt(3, Integer.parseInt(colsStr));
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Η αίθουσα προστέθηκε!");
                clearHallFields();
                loadHallsFromDatabase();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Ελέγξτε τα δεδομένα: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateHall() {
        Hall selectedHall = hallTable.getSelectionModel().getSelectedItem();
        if (selectedHall == null) {
            showAlert(Alert.AlertType.WARNING, "Προσοχή", "Επιλέξτε πρώτα μια αίθουσα από τον πίνακα!");
            return;
        }

        String sql = "UPDATE Halls SET Name = ?, Total_Rows = ?, Total_Cols = ? WHERE Hall_ID = ?";
        try (Connection conn = DBconnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hallNameField.getText().trim());
            pstmt.setInt(2, Integer.parseInt(rowsField.getText().trim()));
            pstmt.setInt(3, Integer.parseInt(colsField.getText().trim()));
            pstmt.setInt(4, selectedHall.getHallId());

            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Η αίθουσα ενημερώθηκε!");
            clearHallFields();
            loadHallsFromDatabase();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Αποτυχία ενημέρωσης: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteHall() {
        Hall selectedHall = hallTable.getSelectionModel().getSelectedItem();
        if (selectedHall == null) {
            showAlert(Alert.AlertType.WARNING, "Προσοχή", "Επιλέξτε πρώτα μια αίθουσα από τον πίνακα!");
            return;
        }

        String sql = "DELETE FROM Halls WHERE Hall_ID = ?";
        try (Connection conn = DBconnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selectedHall.getHallId());
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Η αίθουσα διαγράφηκε!");
            clearHallFields();
            loadHallsFromDatabase();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Αποτυχία διαγραφής: " + e.getMessage());
        }
    }

    private void clearHallFields() {
        hallNameField.clear(); rowsField.clear(); colsField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}