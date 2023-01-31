import java.util.Random;
public class Main {
    public static void main(String[] args) {
        Random random = new Random();
        Individual[] individuals = new Individual[5];
        for (int i = 0; i < 5; i++) {
            if (i == 0) {
                individuals[i] = new Individual(random, i);
            } else {
                individuals[i] = new Individual(random, i, individuals[i - 1]);
            }
            individuals[i].print();
        }
    }
}