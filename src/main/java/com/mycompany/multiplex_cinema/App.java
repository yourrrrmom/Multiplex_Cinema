package com.mycompany.multiplex_cinema;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

// imports για τη ΒΔ
import multiplex.util.DBconnection;
import java.sql.Connection;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Αρχικοποίηση βάσης
        multiplex.util.DBsetup.setup();

        // 2. Δοκιμή σύνδεσης με τη βάση SQLite πριν φορτώσει το γραφικό περιβάλλον
        Connection conn = DBconnection.getConnection();
        if (conn != null) {
            System.out.println("Επιτυχής εκκίνηση! Η βάση δεδομένων είναι έτοιμη..");
        } else {
            System.out.println("Πρόβλημα με τη βάση δεδομένων..");
        }

        // 3. Φόρτωση της αρχικής οθόνης (ψάχνει το αρχείο primary.fxml στον φάκελο resources)
        scene = new Scene(loadFXML("login"), 800, 600);
        
        stage.setTitle("Multiplex Cinema Management System");  // ο τίτλος του παραθύρου
        stage.setScene(scene);
        stage.show();
    }

    // μέθοδος για να αλλάζουμε εύκολα οθόνες
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}