
package multiplex.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import com.mycompany.multiplex_cinema.App;
import java.io.IOException;
import multiplex.util.DBconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    // αυτά τα πεδία συνδέονται με το Scene Builder (τα fx:id)
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    public void handleLoginAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Παρακαλώ συμπληρώστε όλα τα πεδία..");
            return;
        }

        // έλεγχος των στοιχείων στη ΒΔ
        String query = "SELECT Role FROM Users WHERE Username = ? AND Password = ?";
        
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password); // ιδανικά, εδώ συγκρίνουμε Hashed passwords
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String role = rs.getString("Role");
                messageLabel.setText("Επιτυχής σύνδεση! Ρόλος: " + role);
                
                // αλλαγή οθόνης ανάλογα με τον ρόλο του χρήστη
                try {
                    if ("manager".equals(role)) {
                        App.setRoot("manager_dashboard");  // φορτώνει το αρχείο manager_dashboard.fxml
                    
                    } else if ("cashier".equals(role)) {
                        App.setRoot("cashier_dashboard");  // φορτώνει το αρχείο cashier_dashboard.fxml
                    
                    } else {
                        messageLabel.setText("Άγνωστος ρόλος χρήστη.");
                    }
                } catch (IOException ex) {
                    messageLabel.setText("Σφάλμα κατά τη φόρτωση της νέας οθόνης.");
                    ex.printStackTrace();
                }
            } else {
                messageLabel.setText("Λάθος Όνομα Χρήστη ή Κωδικός..");
            }
            
        } catch (SQLException e) {
            messageLabel.setText("Σφάλμα σύνδεσης: " + e.getMessage());
        }
    }
}
