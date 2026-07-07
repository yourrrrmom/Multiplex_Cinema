package multiplex.controller;

import javafx.application.Platform;
import javafx.scene.layout.GridPane;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SeatingControllerTest {

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockPsHall;
    @Mock private PreparedStatement mockPsRes;
    @Mock private ResultSet mockRsHall;
    @Mock private ResultSet mockRsRes;
    @Mock private ScreeningDisplay mockScreening;

    // Ανάβουμε το JavaFX Toolkit για να μπορούν να δημιουργηθούν τα Buttons
    @BeforeAll
    public static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (Exception e) {
            // Το toolkit τρέχει ήδη
        }
    }

    @Test
    public void testInitData_BuildsGridCorrectly() throws Exception {
        SeatingController controller = new SeatingController();

        // Φτιάχνουμε ένα ΑΛΗΘΙΝΟ GridPane για να το γεμίσει ο Controller με κουμπιά
        GridPane realGrid = new GridPane();

        // Εισαγωγή του GridPane στον Controller με Reflection
        java.lang.reflect.Field field = controller.getClass().getDeclaredField("seatGrid");
        field.setAccessible(true);
        field.set(controller, realGrid);

        // Ρυθμίζουμε την ψεύτικη προβολή
        when(mockScreening.getRoom()).thenReturn("Hall A");
        when(mockScreening.getScreeningId()).thenReturn(101);

        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(mockConnection);

            // ΠΡΟΣΟΧΗ: Ξεχωρίζουμε τα δύο διαφορετικά SQL queries με το 'contains'
            when(mockConnection.prepareStatement(contains("Halls"))).thenReturn(mockPsHall);
            when(mockConnection.prepareStatement(contains("Reserved_Seats"))).thenReturn(mockPsRes);

            when(mockPsHall.executeQuery()).thenReturn(mockRsHall);
            when(mockPsRes.executeQuery()).thenReturn(mockRsRes);

            // Απάντηση Βάσης για την Αίθουσα: 2 Γραμμές x 2 Στήλες = 4 Θέσεις συνολικά
            when(mockRsHall.next()).thenReturn(true);
            when(mockRsHall.getInt("Total_Rows")).thenReturn(2);
            when(mockRsHall.getInt("Total_Cols")).thenReturn(2);

            // Απάντηση Βάσης για τις Κρατήσεις: 1 θέση πιασμένη
            when(mockRsRes.next()).thenReturn(true).thenReturn(false);
            when(mockRsRes.getInt("Row_num")).thenReturn(1);
            when(mockRsRes.getInt("Col_num")).thenReturn(1);

            // ΕΚΤΕΛΕΣΗ
            controller.initData(mockScreening);

            // ΕΠΑΛΗΘΕΥΣΕΙΣ
            // 1. Ελέγχουμε αν η Βάση ρωτήθηκε σωστά για το "Hall A" και το ID 101
            verify(mockPsHall).setString(1, "Hall A");
            verify(mockPsRes).setInt(1, 101);

            // 2. Ελέγχουμε αν το GridPane γέμισε με ακριβώς 4 κουμπιά (2x2)
            assertEquals(4, realGrid.getChildren().size(),
                    "Πρέπει να δημιουργηθούν ακριβώς 4 κουμπιά-θέσεις.");
        }
    }
}