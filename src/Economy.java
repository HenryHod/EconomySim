import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;

public class Economy {
    private HashMap<Integer, Family> families;
    public Economy(int size, Random rand) {
        families = new HashMap<>();
        for (int i = 0; i < size; i++) {
             families.put(i, new Family( new Individual(rand, i)));
        }
    }
    public Individual findLeastUtils(Individual self) {

        PriorityQueue<Individual> leastUtilsIndividual = new PriorityQueue<>();
        for (int i = Math.max(0, self.family - 5);i < Math.min(families.size(), self.family + 6); i++) {
            if (i != self.family) {
                leastUtilsIndividual.add(families.get(i).leastUtils());
            }
        }
        return leastUtilsIndividual.poll();
    }
    public Family get(Integer i) {
        return families.get(i);
    }
}
