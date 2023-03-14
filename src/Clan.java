import java.util.*;


public class Clan implements Iterable<Individual>{
    private int size;
    private int living;
    private TreeSet<Individual> individuals;
    private HashMap<Integer, Individual> individualIndex;
    public static class IndividualComparator implements Comparator<Individual> {

        @Override
        public int compare(Individual o1, Individual o2) {
                return o2.id - o1.id;
        }
    }
    public Clan(Individual founder) {
        individuals = new TreeSet<>(new IndividualComparator());
        individualIndex = new HashMap<>();
        size = 1;
        living = 1;
        individuals.add(founder);
        individualIndex.put(founder.id, founder);
    }
    public Individual leastUtils() {
        return individuals.first();
    }
    public void add(Individual i) {
        //System.out.println("before: " + individuals.size());
        individuals.add(i);
        individualIndex.put(i.id, i);
        size++;
        living++;
        //System.out.println("after: " + individuals.size());
        //System.out.println(i.id);
    }
    public void remove(Individual i) {
        individuals.remove(i);
        individualIndex.remove(i.id);
        living -= 1;
    }
    public double totalUtility() {
        return individuals.stream().mapToDouble(Individual::getCurrentUtility).sum();
    }
    public int size() {
        return size;
    }
    public int living() {
        return living;
    }
    public int[] totalGoods() {
        return new int[]{individuals.stream().mapToInt(ind -> ind.goods[0]).sum(), individuals.stream().mapToInt(ind -> ind.goods[0]).sum()};
    }
    public boolean contains(Individual i) {
        return individuals.contains(i);
    }
    public Iterator<Individual> noCopyIterator() {
        return individuals.iterator();
    }
    @Override
    public Iterator<Individual> iterator() {
        TreeSet<Individual> individualsCopy = (TreeSet<Individual>) individuals.clone();
        return individualsCopy.descendingIterator();
    }
    public Individual get(Integer n) {
        return individualIndex.get(n);
    }
}
