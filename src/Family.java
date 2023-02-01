import java.util.Iterator;
import java.util.TreeSet;

public class Family implements Iterable<Individual>{
    private TreeSet<Individual> individuals;
    public Family(Individual founder) {
        individuals = new TreeSet<>();
        individuals.add(founder);
    }
    public Individual leastUtils() {
        return individuals.first();
    }
    public void remove(Individual i) {
        individuals.remove(i);
    }
    public double totalUtility() {
        return individuals.stream().mapToDouble(Individual::getCurrentUtility).sum();
    }
    @Override
    public Iterator<Individual> iterator() {
        return individuals.iterator();
    }
}
