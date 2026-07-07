package multiplex.controller;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import model.ReservationDisplay;
import multiplex.util.DBconnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReservationListControllerTest {

    @Mock private Connection mockConnection;
    @Mock private Statement mockStatement;
    @Mock private PreparedStatement mockPreparedStatement1;
    @Mock private PreparedStatement mockPreparedStatement2;
    @Mock private ResultSet mockResultSet;
    @Mock private CashierController mockCashierController;

    // Εκκίνηση του JavaFX Toolkit για τον πίνακα και τα κουμπιά
    @BeforeAll
    public static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (Exception e) {
            // Το toolkit τρέχει ήδη
        }
    }

    @Test
    public void testLoadReservations_PopulatesTableCorrectly() throws Exception {
        ReservationListController controller = new ReservationListController();

        // Προετοιμασία UI
        TableView<ReservationDisplay> table = new TableView<>();
        setPrivateField(controller, "reservationsTable", table);

        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

            // Η βάση επιστρέφει 1 κράτηση
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getInt("Reservation_ID")).thenReturn(105);
            when(mockResultSet.getString("Title")).thenReturn("Inception");
            when(mockResultSet.getString("Start_Time")).thenReturn("2026-07-15 19:30:00");
            when(mockResultSet.getDouble("Total_Price")).thenReturn(25.50);

            // Εκτέλεση της private μεθόδου loadReservations μέσω Reflection
            invokePrivateMethod(controller, "loadReservations");

            // Επαλήθευση: Ο πίνακας πρέπει να έχει ακριβώς 1 στοιχείο
            assertEquals(1, table.getItems().size(), "Ο πίνακας πρέπει να περιέχει 1 κράτηση.");
            
            // Επαλήθευση: Το συνδυαστικό String (Title + Start_Time) δημιουργήθηκε σωστά;
            ReservationDisplay loaded = table.getItems().get(0);
            assertEquals("Inception (2026-07-15 19:30:00)", loaded.getMovieDetails());
            assertEquals(105, loaded.getReservationId());
        }
    }

    @Test
    public void testHandleDelete_DeletesRecordsAndNotifiesParent() throws Exception {
        ReservationListController controller = new ReservationListController();

        // 1. Προετοιμασία UI (Πίνακας με επιλεγμένη γραμμή)
        TableView<ReservationDisplay> table = new TableView<>();
        ReservationDisplay dummyReservation = new ReservationDisplay(105, "Inception (19:30)", 25.50);
        table.getItems().add(dummyReservation);
        table.getSelectionModel().select(dummyReservation); // Επιλέγουμε την κράτηση
        
        setPrivateField(controller, "reservationsTable", table);
        
        // 2. Σύνδεση με τον "ψεύτικο" Cashier Controller
        controller.setParentController(mockCashierController);

        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(mockConnection);
            
            // Ξεχωρίζουμε τα 2 διαφορετικά Delete Queries
            when(mockConnection.prepareStatement(contains("Reserved_Seats"))).thenReturn(mockPreparedStatement1);
            when(mockConnection.prepareStatement(contains("Reservations"))).thenReturn(mockPreparedStatement2);
            
            // Για το loadReservations που καλείται στο τέλος της διαγραφής
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            // 3. Εκτέλεση της Διαγραφής
            controller.handleDelete();

            // 4. ΕΠΑΛΗΘΕΥΣΕΙΣ
            // Επιβεβαιώνουμε ότι διαγράφηκαν πρώτα οι θέσεις (SQL 1)
            verify(mockPreparedStatement1).setInt(1, 105);
            verify(mockPreparedStatement1).executeUpdate();
            
            // Επιβεβαιώνουμε ότι διαγράφηκε η κράτηση (SQL 2)
            verify(mockPreparedStatement2).setInt(1, 105);
            verify(mockPreparedStatement2).executeUpdate();
            
            // Επιβεβαιώνουμε ότι ο Ταμίας (Cashier) ειδοποιήθηκε να ανανεώσει τις δικές του προβολές
            verify(mockCashierController, times(1)).loadScreeningsFromDatabase();
        }
    }

    // --- Βοηθητικές μέθοδοι Reflection ---
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    private void invokePrivateMethod(Object object, String methodName) throws Exception {
        Method method = object.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(object);
    }
}