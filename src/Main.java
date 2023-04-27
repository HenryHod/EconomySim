import java.util.Random;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class Main {
    public static void main(String[] args) throws SQLException {
        Random random = new Random();
        //Class.forName("org.sqlite.JDBC");
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:simulation.db");
            conn.setAutoCommit(false);
            statement = conn.createStatement();
            // Do something with the Connection
            statement.executeUpdate("""
                    DROP TABLE IF EXISTS simulations
                    """);
            statement.executeUpdate("""
                    CREATE TABLE simulations (
                    id INTEGER PRIMARY KEY,
                    sim_id INTEGER NOT NULL,
                    period INTEGER NOT NULL,
                    clan INTEGER NOT NULL,
                    family INTEGER NOT NULL,
                    generation INTEGER NOT NULL,
                    mean_altruism DOUBLE NOT NULL,
                    mean_patience DOUBLE NOT NULL,
                    mean_charity DOUBLE NOT NULL,
                    std DOUBLE NOT NULL,
                    age INTEGER NOT NULL,
                    children INTEGER NOT NULL,
                    prev_children INTEGER NOT NULL,
                    altruism INTEGER NOT NULL,
                    patience INTEGER NOT NULL,
                    charity INTEGER NOT NULL,
                    goods INTEGER NOT NULL,
                    future_goods INTEGER NOT NULL,
                    self_goods INTEGER NOT NULL,
                    char_goods INTEGER NOT NULL,
                    pref INTEGER NOT NULL,
                    utility DOUBLE NOT NULL
                    )
                    """);
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        int maxA = 1;
        int maxB = 1;
        int maxC = 6;
        int maxD = 2;
        double sd = 0.05;
        double altruism = 0.636;
        double patience = 0.697;
        double charity = 0.256;
        int sampleSize = 10;
        for (int a = 0; a < maxA; a++) {
            for (int b = 0; b < maxB; b++) {
                for (int c = 0; c < maxC; c++) {
                    for (int d = 0; d < maxD; d++) {
                        Economy economy = new Economy((int) Math.pow(sampleSize, c), random, statement, altruism, patience, charity, sd, 5, ((c - 1) * maxD) + d);
                        for (int i = 0; i < 5; i++) {
                            economy.period();
                            //economy.print();
                            //System.out.println(altruism * a + " " + patience * b + " " + sd * c);
                            //int percent = (c * maxA * maxB) + (b * maxA) + a;
                            
                        }
                        conn.commit();
                        System.out.println(a + " " + b + " " + c + " " + d);
                    }
                }                
            }
        }
    }
}