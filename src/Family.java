
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import static java.lang.Double.NEGATIVE_INFINITY;

public class Family implements Iterable<Individual> {
    private int size;
    public static class IndividualComparator implements Comparator<Individual> {
        private int smallComp(double double1, double double2) {
            return Double.compare(double1, double2);
        }

        @Override
        public int compare(Individual o1, Individual o2) {
            if (o1.parent != null || o2.parent != null) {
                if (o1.isChild(o2) | o1.isGrandchild(o2)) {
                    return (int) NEGATIVE_INFINITY;
                } else if (o2.isChild(o1) | o2.isGrandchild(o1)) {
                    return (int) Double.POSITIVE_INFINITY;
                } else if (o1.isSibling(o2)) {
                    return (int) NEGATIVE_INFINITY;
                }
            } else if (o1.getCurrentUtility() == 0 && o2.getCurrentUtility() == 0) {
                return o1.id - o2.id;
            }
            return smallComp(o1.getCurrentUtility(), o2.getCurrentUtility());
        }
    }
    private TreeSet<Individual> individuals;
    public Family(Individual founder) {
        individuals = new TreeSet<>(new IndividualComparator());
        size = 1;
        individuals.add(founder);
    }
    public Individual leastUtils() {
        return individuals.first();
    }
    public void add(Individual i) {
        //System.out.println("before: " + individuals.size());
        individuals.add(i);
        size++;
        //System.out.println("after: " + individuals.size());
        //System.out.println(i.id);
    }
    public void remove(Individual i) {
        i.removeSelf();
        individuals.remove(i);
    }
    public double totalUtility() {
        return individuals.stream().mapToDouble(Individual::getCurrentUtility).sum();
    }
    public int size() {
        return size;
    }
    public int living() {
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
