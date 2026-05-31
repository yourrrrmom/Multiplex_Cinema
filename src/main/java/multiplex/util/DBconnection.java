
package multiplex.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnection {
    // το αρχείο multiplex.db πρέπει να βρίσκεται στον κεντρικό φάκελο του NetBeans project
    private static final String URL = "jdbc:sqlite:multiplex.db";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            System.out.println("Επιτυχής σύνδεση με SQLite!");
        } catch (SQLException e) {
            System.out.println("Σφάλμα σύνδεσης με τη βάση: " + e.getMessage());
        }
        return conn;
    }
}
