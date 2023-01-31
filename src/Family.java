import java.util.TreeSet;

public class Family {
    private TreeSet<Individual> individuals;
    public Family(Individual founder) {
        individuals = new TreeSet<>();
        individuals.add(founder);
    }
    public Individual leastUtils() {
        return individuals.first();
    }
}
