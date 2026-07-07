package multiplex.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import model.ScreeningDisplay;
import multiplex.util.DBconnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CashierControllerTest {

    @Mock private Connection mockConnection;
    @Mock private Statement mockStatement;
    @Mock private ResultSet mockResultSet;

    private CashierController controller;

    // Ανάβουμε το JavaFX Toolkit (όπως έκανε η ομάδα σου)
    @BeforeAll
    public static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (Exception e) {
            // Το toolkit τρέχει ήδη
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new CashierController();
        // Το screeningList είναι final, αλλά εμείς απλά θα το αδειάσουμε πριν από κάθε test
        ObservableList<ScreeningDisplay> list = getPrivateField(controller, "screeningList");
        list.clear();
    }

    @Test
    public void testLoadScreeningsFromDatabase_PopulatesListCorrectly() throws Exception {
        // Προετοιμασία των ψεύτικων δεδομένων (Mocking)
        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

            // Προσομοιώνουμε ότι η βάση επιστρέφει 2 προβολές και μετά σταματάει (false)
            when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

            // Δεδομένα 1ης προβολής
            when(mockResultSet.getInt("Screening_ID")).thenReturn(101).thenReturn(102);
            when(mockResultSet.getString("Title")).thenReturn("Inception").thenReturn("Matrix");
            when(mockResultSet.getString("Start_Time")).thenReturn("2026-07-15 19:30").thenReturn("2026-07-16 21:00");
            when(mockResultSet.getString("Room_Name")).thenReturn("Αίθουσα 1").thenReturn("Αίθουσα 2");
            when(mockResultSet.getInt("Available_Seats")).thenReturn(50).thenReturn(30);
            when(mockResultSet.getDouble("Ticket_Price")).thenReturn(8.50).thenReturn(7.00);
            when(mockResultSet.getString("Poster_path")).thenReturn("/images/inception.png").thenReturn("/images/matrix.png");

            // 2. Εκτέλεση (Act)
            controller.loadScreeningsFromDatabase();

            // 3. Επαλήθευση (Assert)
            ObservableList<ScreeningDisplay> list = getPrivateField(controller, "screeningList");
            
            // Ελέγχουμε αν δημιουργήθηκαν ακριβώς 2 αντικείμενα
            assertEquals(2, list.size(), "Πρέπει να φορτωθούν 2 προβολές από τη βάση.");

            // Ελέγχουμε τα στοιχεία της 1ης προβολής
            ScreeningDisplay firstScreening = list.get(0);
            assertEquals(101, firstScreening.getScreeningId());
            assertEquals("Inception", firstScreening.getTitle());
            assertEquals(50, firstScreening.getAvailableSeats());
            assertEquals("/images/inception.png", firstScreening.getPosterPath());

            // Επιβεβαιώνουμε ότι το SQL query έτρεξε πράγματι
            verify(mockStatement).executeQuery(anyString());
        }
    }

    // Βοηθητική μέθοδος (ίδια με αυτή που έχει ήδη η ομάδα σου)
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }
}
