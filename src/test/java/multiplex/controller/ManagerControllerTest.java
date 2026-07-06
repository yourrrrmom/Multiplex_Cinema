
package multiplex.controller;

import multiplex.model.Screening;
import multiplex.util.DBconnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagerControllerTest {

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockPreparedStatement;
    @Mock private ResultSet mockResultSet;

    @Test
    public void testHasConflict_ReturnsTrue_WhenScreeningsOverlap() throws Exception {
        ManagerController controller = new ManagerController();
        
        // 1. Ρυθμίζουμε τη Mock Βάση Δεδομένων
        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            // 2. Λέμε στο ResultSet να επιστρέψει μια "εικονική" προβολή που ήδη υπάρχει
            when(mockResultSet.next()).thenReturn(true).thenReturn(false); 
            when(mockResultSet.getInt("Screening_ID")).thenReturn(2);
            when(mockResultSet.getString("Start_Time")).thenReturn("2026-06-15 19:00:00");
            when(mockResultSet.getInt("Duration_mins")).thenReturn(120); // Τελειώνει 21:00 + 15' = 21:15

            // 3. Δημιουργούμε μια υποψήφια προβολή που ξεκινάει στις 20:00 (Ξεκάθαρη σύγκρουση)
            Screening candidate = new Screening(-1, 1, 1, "2026-06-15 20:00:00", 8.50);

            // 4. Εκτέλεση του Business Logic
            boolean result = controller.hasConflict(candidate, -1);

            // 5. Έλεγχος Αποτελέσματος: Πρέπει να επιστρέψει true (υπάρχει conflict)
            assertTrue(result, "Θα έπρεπε να εντοπιστεί σύγκρουση ωραρίου, διότι οι ώρες αλληλεπικαλύπτονται.");
        }
    }

    @Test
    public void testHasConflict_ReturnsFalse_WhenNoOverlap() throws Exception {
        ManagerController controller = new ManagerController();

        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            // Η υπάρχουσα προβολή ξεκινάει 15:00 και διαρκεί 120 λεπτά (17:15)
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getInt("Screening_ID")).thenReturn(2);
            when(mockResultSet.getString("Start_Time")).thenReturn("2026-06-15 15:00:00");
            when(mockResultSet.getInt("Duration_mins")).thenReturn(120);

            // Η νέα προβολή ξεκινάει 20:00 (Καμία σύγκρουση)
            Screening candidate = new Screening(-1, 1, 1, "2026-06-15 20:00:00", 8.50);

            boolean result = controller.hasConflict(candidate, -1);

            assertFalse(result, "Δεν θα έπρεπε να υπάρχει σύγκρουση ωραρίου.");
        }
    }
}
