import java.util.Random;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class Main {
    public static void main(String[] args) throws SQLException {
        Random random = new Random(10);
        //Class.forName("org.sqlite.JDBC");
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:simulation.db");
            conn.setAutoCommit(false);
            statement = conn.createStatement();
            // Do something with the Connection
            statement.executeUpdate("""
                    DROP TABLE IF EXISTS simulations;
                    DROP TABLE IF EXISTS economies
                    """);
            statement.executeUpdate("""
                    CREATE TABLE simulations (
                    id INTEGER PRIMARY KEY,
                    sim_id INTEGER NOT NULL,
                    period INTEGER NOT NULL,
                    clan INTEGER NOT NULL,
                    family INTEGER NOT NULL,
                    generation INTEGER NOT NULL,
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
                    utility DOUBLE NOT NULL);
                    
                    CREATE TABLE economies (
                    id INTEGER PRIMARY KEY,
                    sim_id INTEGER NOT NULL,
                    period INTEGER NOT NULL,
                    start_population INTEGER NOT NULL,
                    population INTEGER NOT NULL,
                    goods INTEGER NOT NULL,
                    future_goods INTEGER NOT NULL,
                    self_goods INTEGER NOT NULL,
                    char_goods INTEGER NOT NULL,
                    mean_altruism DOUBLE NOT NULL,
                    mean_patience DOUBLE NOT NULL,
                    mean_charity DOUBLE NOT NULL)
                    """);
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        int maxA = 20;
        int maxB = 10;
        int maxC = 10;
        int maxD = 1;
        double sd = 0.05;
        double minChar = 0.1;
        double minPat = 0.1;
        double altruism = (double) 0.9 / maxA;
        double patience = (double) 0.3 / maxB;
        double charity = (double) 0.3 / maxC;
        int sampleSize = 5;
        int numFinished = 0;
        for (int a = 0; a < maxA + 1; a++) {
            for (int b = 0; b < maxB + 1; b++) {
                for (int c = 0; c < maxC + 1; c++) {
                    for (int d = 0; d < maxD + 1; d++) {
                        Economy economy = new Economy(5000, random, statement, altruism * a, minPat + patience * b, minChar + charity * c, sd, 5, d);
                        for (int i = 0; i < 50; i++) {
                            economy.aggPeriod();
                            //economy.print();
                            //System.out.println(altruism * a + " " + patience * b + " " + sd * c);
                            //int percent = (c * maxA * maxB) + (b * maxA) + a;
                        }
                        numFinished++;
                        //System.out.println((a + " " + b + " " + c + " " + d) + " \r");
                        System.out.print((Math.round((100 * (double) numFinished / ((maxA + 1) * (maxB + 1) * (maxC + 1) * (maxD + 1))) * 100.0) / 100.0) + "% Done \r");
                    }
                    conn.commit();
                }                
            }
        }
    }
}