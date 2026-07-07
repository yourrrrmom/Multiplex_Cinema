package multiplex.controller;

import javafx.scene.control.Label;
import model.ScreeningDisplay;
import multiplex.util.DBconnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationControllerTest {

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockPreparedStatement;
    @Mock private ResultSet mockResultSet;

    // Πρέπει να ξεκινήσουμε το JavaFX Toolkit κενό για να μην χτυπήσουν τα Labels
    @BeforeAll
    public static void initJFX() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (Exception e) {
            // Το toolkit είναι ήδη ανοιχτό, δεν χρειάζεται να κάνουμε τίποτα
        }
    }

    @Test
    public void testHandleFinalizePayment_SuccessfulTransaction() throws Exception {
        ReservationController controller = new ReservationController();
        
        // Αρχικοποίηση εικονικών δεδομένων για το Test
        ScreeningDisplay dummyScreening = new ScreeningDisplay(10, "Test Movie", "19:00", "Hall 1", 50, 8.50, "/path");
        List<String> dummySeats = Arrays.asList("1-1", "1-2");
        
        // Αντί για FXML injection, βάζουμε εμείς τα αντικείμενα (Απομόνωση UI)
        setPrivateField(controller, "movieTitleLabel", new Label());
        setPrivateField(controller, "detailsLabel", new Label());
        setPrivateField(controller, "totalLabel", new Label());

        controller.initReservation(dummyScreening, dummySeats, 17.00);

        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(mockConnection);
            
            // Ρύθμιση συμπεριφοράς Mock
            when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                    .thenReturn(mockPreparedStatement);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            
            when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt(1)).thenReturn(999); // Το Reservation_ID που γυρίζει η βάση

            // Εκτέλεση της μεθόδου Πληρωμής
            controller.handleFinalizePayment();

            // ΕΠΑΛΗΘΕΥΣΕΙΣ (Asserts) - Το σημαντικότερο κομμάτι!
            // 1. Επαλήθευση ότι ξεκίνησε το Transaction
            verify(mockConnection, times(1)).setAutoCommit(false);
            
            // 2. Επαλήθευση ότι προστέθηκαν 2 θέσεις στο Batch (αφού διαλέξαμε 2)
            verify(mockPreparedStatement, times(2)).addBatch();
            
            // 3. Επαλήθευση ότι έγινε Commit στο τέλος
            verify(mockConnection, times(1)).commit();
        }
    }

    @Test
    public void testHandleFinalizePayment_FailureTriggersRollback() throws Exception {
        ReservationController controller = new ReservationController();
        
        // 1. Προετοιμασία ίδιων εικονικών δεδομένων
        ScreeningDisplay dummyScreening = new ScreeningDisplay(10, "Test Movie", "19:00", "Hall 1", 50, 8.50, "/path");
        List<String> dummySeats = Arrays.asList("1-1", "1-2");
        
        setPrivateField(controller, "movieTitleLabel", new Label());
        setPrivateField(controller, "detailsLabel", new Label());
        setPrivateField(controller, "totalLabel", new Label());

        controller.initReservation(dummyScreening, dummySeats, 17.00);

        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(mockConnection);
            
            when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                    .thenReturn(mockPreparedStatement);
            
            // 2. ΠΡΟΣΟΜΟΙΩΣΗ ΑΠΟΤΥΧΙΑΣ: 
            // Λέμε στο σύστημα να "πετάξει" ένα SQL Σφάλμα όταν προσπαθήσει να κάνει το Insert
            when(mockPreparedStatement.executeUpdate()).thenThrow(new java.sql.SQLException("Simulated Database Crash!"));

            // 3. Εκτέλεση (Αναμένουμε το πρόγραμμα να "πιάσει" το λάθος στο catch block)
            controller.handleFinalizePayment();

            // 4. ΕΠΑΛΗΘΕΥΣΕΙΣ (Asserts) - Η απόδειξη ότι το σύστημά μας είναι ασφαλές
            
            // Επιβεβαιώνουμε ότι η συναλλαγή ξεκίνησε κανονικά
            verify(mockConnection, times(1)).setAutoCommit(false);
            
            // Επιβεβαιώνουμε ότι ΑΚΥΡΩΘΗΚΑΝ ΟΛΑ (Rollback) λόγω του σφάλματος
            verify(mockConnection, times(1)).rollback();
            
            // Επιβεβαιώνουμε ότι ΔΕΝ έγινε ποτέ commit στη βάση
            verify(mockConnection, never()).commit();
        }
    }

    // Βοηθητική μέθοδος για να αποφύγουμε τα NullPointerExceptions στα Labels
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}