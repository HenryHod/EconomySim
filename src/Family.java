
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import static java.lang.Double.NEGATIVE_INFINITY;

public class Family implements Iterable<Individual>{
    public static class IndividualComparator implements Comparator<Individual> {

        @Override
        public int compare(Individual o1, Individual o2) {
            if (o1.isChild(o2) | o1.isGrandchild(o2)) {
                return (int) NEGATIVE_INFINITY;
            } else if (o2.isChild(o1) | o2.isGrandchild(o1)) {
                return (int) Double.POSITIVE_INFINITY;
            }
            if ((o1.getCurrentUtility() == 0.0 & o1.getCurrentUtility() == 0.0) | (o1.getCurrentUtility() == o1.getCurrentUtility())) {
                if (o1.goodTotals() == 0 & o2.goodTotals() == 0) {
                    if (o1.children.size() == 0 & o2.children.size() == 0) {
                        if (o1.skills == o2.skills) {
                            return (int) (o1.altruism - o2.altruism);
                        }
                        return o1.skills - o2.skills;
                    }
                    return o1.children.size() - o2.children.size();
                }
                return (int) (100 * o1.potentialUtility() - 100 * o2.potentialUtility());
            }
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
