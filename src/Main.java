import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
public class Main {
    public static void main(String[] args) throws IOException {
        JSONObject jo = new JSONObject("{periods:{}}");
        JSONObject root = new JSONObject(new String(Files.readAllBytes(Paths.get("EconomySimData.json"))));
        JSONObject val_older = root.getJSONObject("periods");
        Random random = new Random();
        Economy economy = new Economy(10, random, jo);
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