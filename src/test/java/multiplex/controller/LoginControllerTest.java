package multiplex.controller;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockPreparedStatement;
    @Mock private ResultSet mockResultSet;

    // Ανάβουμε το JavaFX Toolkit για να μην σκάσουν τα TextFields
    @BeforeAll
    public static void initJFX() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (Exception e) {
            // Το toolkit τρέχει ήδη
        }
    }

    @Test
    public void testHandleLoginAction_EmptyFields_ShowsWarning() throws Exception {
        LoginController controller = new LoginController();

        // Δημιουργία ψεύτικων γραφικών στοιχείων (UI)
        TextField userField = new TextField("");
        PasswordField passField = new PasswordField();
        passField.setText("");
        Label msgLabel = new Label();

        // Εισαγωγή των στοιχείων στον Controller με Reflection
        setPrivateField(controller, "usernameField", userField);
        setPrivateField(controller, "passwordField", passField);
        setPrivateField(controller, "messageLabel", msgLabel);

        // Εκτέλεση μεθόδου
        controller.handleLoginAction();

        // Επαλήθευση ότι το σύστημα σταμάτησε τον χρήστη
        assertEquals("Παρακαλώ συμπληρώστε όλα τα πεδία..", msgLabel.getText(),
                "Το σύστημα πρέπει να ζητάει συμπλήρωση όλων των πεδίων.");
    }

    @Test
    public void testHandleLoginAction_InvalidCredentials_ShowsError() throws Exception {
        LoginController controller = new LoginController();

        // Βάζουμε λάθος στοιχεία
        TextField userField = new TextField("wrongUser");
        PasswordField passField = new PasswordField();
        passField.setText("wrongPass");
        Label msgLabel = new Label();

        setPrivateField(controller, "usernameField", userField);
        setPrivateField(controller, "passwordField", passField);
        setPrivateField(controller, "messageLabel", msgLabel);

        // Mocking της Βάσης Δεδομένων
        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            // Η βάση επιστρέφει false (Δεν βρέθηκε τέτοιος χρήστης)
            when(mockResultSet.next()).thenReturn(false);

            // Εκτέλεση
            controller.handleLoginAction();

            // Επαληθεύσεις
            assertEquals("Λάθος Όνομα Χρήστη ή Κωδικός..", msgLabel.getText(),
                    "Πρέπει να εμφανιστεί το σωστό μήνυμα λάθους.");

            // Ελέγχουμε αν πράγματι πήγε να ψάξει αυτούς τους κωδικούς
            verify(mockPreparedStatement).setString(1, "wrongUser");
            verify(mockPreparedStatement).setString(2, "wrongPass");
        }
    }

    // Βοηθητική μέθοδος (ίδια με του Reservation)
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}