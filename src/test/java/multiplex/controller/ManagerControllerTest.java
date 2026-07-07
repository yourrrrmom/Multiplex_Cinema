package multiplex.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import multiplex.model.Hall;
import multiplex.model.Movie;
import multiplex.model.Screening;
import multiplex.util.DBconnection;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagerControllerTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    private ManagerController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new ManagerController();

        setPrivateField(controller, "movieList", FXCollections.observableArrayList(
                new Movie(1, "Inception", "Sci-fi movie", 120, "Sci-Fi", "/images/inception.jpg"),
                new Movie(2, "Short Film", "Short movie", 60, "Drama", "/images/short.jpg")
        ));

        setPrivateField(controller, "hallList", FXCollections.observableArrayList(
                new Hall(1, "Hall 1", 5, 6)
        ));

        setPrivateField(controller, "screeningList", FXCollections.observableArrayList());
    }

    @Test
    void loadMoviesFromDatabase_loadsAllMoviesIntoMovieList() throws Exception {
        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(connection);

            when(connection.createStatement()).thenReturn(statement);
            when(statement.executeQuery("SELECT * FROM Movies")).thenReturn(resultSet);

            when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

            when(resultSet.getInt("Movie_ID")).thenReturn(10).thenReturn(11);
            when(resultSet.getString("Title")).thenReturn("Movie A").thenReturn("Movie B");
            when(resultSet.getString("Description")).thenReturn("Desc A").thenReturn("Desc B");
            when(resultSet.getInt("Duration_mins")).thenReturn(100).thenReturn(90);
            when(resultSet.getString("Genre")).thenReturn("Drama").thenReturn("Comedy");
            when(resultSet.getString("Poster_path")).thenReturn("a.jpg").thenReturn("b.jpg");

            invokePrivateMethod(controller, "loadMoviesFromDatabase");

            ObservableList<Movie> movies = getPrivateField(controller, "movieList");

            assertEquals(2, movies.size());

            assertEquals(10, movies.get(0).getMovieId());
            assertEquals("Movie A", movies.get(0).getTitle());
            assertEquals("Desc A", movies.get(0).getDescription());
            assertEquals(100, movies.get(0).getDuration());
            assertEquals("Drama", movies.get(0).getGenre());
            assertEquals("a.jpg", movies.get(0).getPosterPath());

            assertEquals(11, movies.get(1).getMovieId());
            assertEquals("Movie B", movies.get(1).getTitle());
            assertEquals(90, movies.get(1).getDuration());

            verify(connection).createStatement();
            verify(statement).executeQuery("SELECT * FROM Movies");
        }
    }

    @Test
    void loadHallsFromDatabase_loadsAllHallsIntoHallList() throws Exception {
        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(connection);

            when(connection.createStatement()).thenReturn(statement);
            when(statement.executeQuery("SELECT * FROM Halls")).thenReturn(resultSet);

            when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

            when(resultSet.getInt("Hall_ID")).thenReturn(1).thenReturn(2);
            when(resultSet.getString("Name")).thenReturn("Hall A").thenReturn("Hall B");
            when(resultSet.getInt("Total_Rows")).thenReturn(5).thenReturn(7);
            when(resultSet.getInt("Total_Cols")).thenReturn(6).thenReturn(8);

            invokePrivateMethod(controller, "loadHallsFromDatabase");

            ObservableList<Hall> halls = getPrivateField(controller, "hallList");

            assertEquals(2, halls.size());

            assertEquals(1, halls.get(0).getHallId());
            assertEquals("Hall A", halls.get(0).getName());
            assertEquals(5, halls.get(0).getTotalRows());
            assertEquals(6, halls.get(0).getTotalCols());

            assertEquals(2, halls.get(1).getHallId());
            assertEquals("Hall B", halls.get(1).getName());
            assertEquals(7, halls.get(1).getTotalRows());
            assertEquals(8, halls.get(1).getTotalCols());

            verify(connection).createStatement();
            verify(statement).executeQuery("SELECT * FROM Halls");
        }
    }

    @Test
    void loadScreeningsFromDatabase_loadsScreeningsWithMovieAndHallNames() throws Exception {
        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(connection);

            when(connection.createStatement()).thenReturn(statement);
            when(statement.executeQuery(anyString())).thenReturn(resultSet);

            when(resultSet.next()).thenReturn(true).thenReturn(false);

            when(resultSet.getInt("Screening_ID")).thenReturn(5);
            when(resultSet.getInt("Movie_ID")).thenReturn(1);
            when(resultSet.getInt("Hall_ID")).thenReturn(1);
            when(resultSet.getString("Start_Time")).thenReturn("2026-06-15 20:00:00");
            when(resultSet.getDouble("Ticket_Price")).thenReturn(8.50);
            when(resultSet.getString("Movie_Title")).thenReturn("Inception");
            when(resultSet.getString("Hall_Name")).thenReturn("Hall 1");

            invokePrivateMethod(controller, "loadScreeningsFromDatabase");

            ObservableList<Screening> screenings = getPrivateField(controller, "screeningList");

            assertEquals(1, screenings.size());

            Screening screening = screenings.get(0);

            assertEquals(5, screening.getScreeningId());
            assertEquals(1, screening.getMovieId());
            assertEquals(1, screening.getHallId());
            assertEquals("2026-06-15 20:00:00", screening.getStartTime());
            assertEquals(8.50, screening.getTicketPrice(), 0.001);
            assertEquals("Inception", screening.getMovieTitle());
            assertEquals("Hall 1", screening.getHallName());

            verify(connection).createStatement();
            verify(statement).executeQuery(anyString());
        }
    }

    @Test
    void hasConflict_returnsTrue_whenCandidateStartsDuringExistingScreening() throws Exception {
        /*
         * Existing:
         * 19:00 - 21:15
         *
         * Candidate:
         * 20:00 - 22:15
         *
         * Άρα υπάρχει conflict.
         */
        Screening candidate = new Screening(-1, 1, 1, "2026-06-15 20:00:00", 8.50);

        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockOneExistingScreening(
                    mockedDb,
                    10,
                    "2026-06-15 19:00:00",
                    120
            );

            boolean result = controller.hasConflict(candidate, -1);

            assertTrue(result);
            verify(preparedStatement).setInt(1, 1);
        }
    }

    @Test
    void hasConflict_returnsFalse_whenCandidateStartsAfterExistingMoviePlusBuffer() throws Exception {
        /*
         * Existing:
         * 19:00 - 21:15
         *
         * Candidate:
         * 21:15 - 23:30
         *
         * Ξεκινάει ακριβώς μετά τη διάρκεια + buffer.
         * Άρα δεν υπάρχει conflict.
         */
        Screening candidate = new Screening(-1, 1, 1, "2026-06-15 21:15:00", 8.50);

        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockOneExistingScreening(
                    mockedDb,
                    10,
                    "2026-06-15 19:00:00",
                    120
            );

            boolean result = controller.hasConflict(candidate, -1);

            assertFalse(result);
            verify(preparedStatement).setInt(1, 1);
        }
    }

    @Test
    void hasConflict_returnsFalse_whenCandidateEndsExactlyWhenExistingStarts() throws Exception {
        /*
         * Candidate:
         * 17:00 - 19:15
         *
         * Existing:
         * 19:15 - 21:30
         *
         * Δεν υπάρχει overlap.
         */
        Screening candidate = new Screening(-1, 1, 1, "2026-06-15 17:00:00", 8.50);

        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockOneExistingScreening(
                    mockedDb,
                    10,
                    "2026-06-15 19:15:00",
                    120
            );

            boolean result = controller.hasConflict(candidate, -1);

            assertFalse(result);
            verify(preparedStatement).setInt(1, 1);
        }
    }

    @Test
    void hasConflict_returnsFalse_whenUpdatingSameScreening() throws Exception {
        /*
         * Όταν γίνεται update της ίδιας προβολής,
         * πρέπει να αγνοείται το ίδιο Screening_ID.
         */
        Screening candidate = new Screening(10, 1, 1, "2026-06-15 19:00:00", 8.50);

        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(connection);

            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);

            when(resultSet.next()).thenReturn(true).thenReturn(false);
            when(resultSet.getInt("Screening_ID")).thenReturn(10);

            boolean result = controller.hasConflict(candidate, 10);

            assertFalse(result);
            verify(preparedStatement).setInt(1, 1);
            verify(resultSet, atLeastOnce()).getInt("Screening_ID");
        }
    }

    @Test
    void hasConflict_returnsFalse_whenThereAreNoScreeningsInSameHall() throws Exception {
        Screening candidate = new Screening(-1, 1, 1, "2026-06-15 20:00:00", 8.50);

        try (MockedStatic<DBconnection> mockedDb = mockStatic(DBconnection.class)) {
            mockedDb.when(DBconnection::getConnection).thenReturn(connection);

            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);

            when(resultSet.next()).thenReturn(false);

            boolean result = controller.hasConflict(candidate, -1);

            assertFalse(result);
            verify(preparedStatement).setInt(1, 1);
        }
    }

    @Test
    void getMovieDuration_returnsCorrectDuration_whenMovieExists() {
        int duration = controller.getMovieDuration(1);

        assertEquals(120, duration);
    }

    @Test
    void getMovieDuration_returnsZero_whenMovieDoesNotExist() {
        int duration = controller.getMovieDuration(999);

        assertEquals(0, duration);
    }

    private void mockOneExistingScreening(
            MockedStatic<DBconnection> mockedDb,
            int screeningId,
            String startTime,
            int durationMinutes
    ) throws Exception {
        mockedDb.when(DBconnection::getConnection).thenReturn(connection);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("Screening_ID")).thenReturn(screeningId);
        when(resultSet.getString("Start_Time")).thenReturn(startTime);
        when(resultSet.getInt("Duration_mins")).thenReturn(durationMinutes);
    }

    private Object invokePrivateMethod(Object target, String methodName) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(target);
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}