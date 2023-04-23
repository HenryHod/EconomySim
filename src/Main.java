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
                    period INTEGER NOT NULL,
                    clan INTEGER NOT NULL,
                    family INTEGER NOT NULL,
                    generation INTEGER NOT NULL,
                    mean_altruism DOUBLE NOT NULL,
                    mean_patience DOUBLE NOT NULL,
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
        int maxA = 10;
        int maxB = 10;
        int maxC = 10;
<<<<<<< HEAD
<<<<<<< HEAD
        int maxD = 10;
        double sd = 0.05;
        double altruism = 1;//(double) 1 / maxA;
        double patience = 1;//(double) 1 / maxB;
        double charity = (double) 1 / maxC;
        for (int a = 0; a < maxA + 1; a++) {
            for (int b = 0; b < maxB + 1; b++) {
                for (int c = 0; c < maxC + 1; c++) {
                    for (int d = 0; d < maxD + 1; d++) {
                        Economy economy = new Economy(1, random, statement, altruism, patience, charity * c, sd, 5, d);
                        for (int i = 0; i < 25; i++) {
                            //System.out.println(i);
                            economy.period();
                            //economy.print();
                            //System.out.println(altruism * a + " " + patience * b + " " + sd * c);
                            //int percent = (c * maxA * maxB) + (b * maxA) + a;
                        }
                        System.out.println(a + " " + b + " " + c + " " + d);
=======
        double sd = 0.05;
        double altruism = (double) 1 / maxA;
        double patience = (double) 1 / maxB;
        for (int a = 0; a < maxA + 1; a++) {
            for (int b = 0; b < maxB + 1; b++) {
                for (int c = 0; c < maxC + 1; c++) {
=======
        double sd = 0.05;
        double altruism = (double) 1 / maxA;
        double patience = (double) 1 / maxB;
        for (int a = 0; a < maxA + 1; a++) {
            for (int b = 0; b < maxB + 1; b++) {
                for (int c = 0; c < maxC + 1; c++) {
>>>>>>> parent of 3cae6a7 (commit 23)
                    Economy economy = new Economy(1000, random, statement, altruism * a, patience * b, sd, 5);
                    for (int i = 0; i < 30; i++) {
                        economy.period();
                        //economy.print();
                        //System.out.println(altruism * a + " " + patience * b + " " + sd * c);
                        //int percent = (c * maxA * maxB) + (b * maxA) + a;
<<<<<<< HEAD
>>>>>>> parent of 3cae6a7 (commit 23)
=======
>>>>>>> parent of 3cae6a7 (commit 23)
                    }
                    conn.commit();
                }
            }
        }
    }
}