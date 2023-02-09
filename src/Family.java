import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class Family implements Iterable<Individual>{
    public class IndividualComparator implements Comparator<Individual> {

        @Override
        public int compare(Individual o1, Individual o2) {
            return (int) (100 * o1.getCurrentUtility() - 100 * o2.getCurrentUtility());
        }
    }
    private TreeSet<Individual> individuals;
    public Family(Individual founder) {
        individuals = new TreeSet<>(new IndividualComparator());
        individuals.add(founder);
    }
    public Individual leastUtils() {
        return individuals.first();
    }
    public void add(Individual i) {
        individuals.add(i);
    }
    public void remove(Individual i) {
        i.removeSelf();
        individuals.remove(i);
    }
    public double totalUtility() {
        return individuals.stream().mapToDouble(Individual::getCurrentUtility).sum();
    }
    public int size() {
        return individuals.size();
    }
    public int[] totalGoods() {
        return new int[]{individuals.stream().mapToInt(ind -> ind.goods[0]).sum(), individuals.stream().mapToInt(ind -> ind.goods[0]).sum()};
    }
    @Override
    public Iterator<Individual> iterator() {
        TreeSet<Individual> individualsCopy = (TreeSet<Individual>) individuals.clone();
        return individualsCopy.iterator();
    }
}
