import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        JSONObject jo = new JSONObject("{periods:{}}");
        JSONObject root = new JSONObject(new String(Files.readAllBytes(Paths.get("EconomySimData.json"))));
        JSONObject val_older = root.getJSONObject("periods");
        Random random = new Random();
        Economy economy = new Economy(10000, random, jo);
        //Class.forName("org.sqlite.JDBC");
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:simulation.db");
            Statement statement = conn.createStatement();
            // Do something with the Connection
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS simulations (
                    id INTEGER PRIMARY KEY,
                    period INTEGER NOT NULL,
                    family INTEGER NOT NULL,
                    generation INTEGER NOT NULL,
                    children INTEGER NOT NULL,
                    altruism INTEGER NOT NULL,
                    charity INTEGER NOT NULL,
                    
                    
                    )
                    """);
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        for (int i = 0; i < 15; i++) {
            economy.period();
            economy.print();
        }
        JSONObject val_newer = jo.getJSONObject("periods");
        if(!val_newer.equals(val_older)) {
            //Update value in object
            root.put("periods",val_newer);

            //Write into the file
            try (FileWriter file = new FileWriter("EconomySimData.json")) {
                file.write(root.toString());
                System.out.println("Successfully updated json object to file...!!");
            }
        }
    }
}