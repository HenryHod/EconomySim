import java.util.Random;
public class Main {
    public static void main(String[] args) {
        Random random = new Random();
        Economy economy = new Economy(15, random);
        for (int i = 0; i < 5; i++) {
            economy.print();
            economy.period();
            economy.print();
        }
    }
}