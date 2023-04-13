import java.util.*;


public class Clan implements Iterable<Individual>{
    private int id;
    private int size;
    private int living;
    private HashMap<Integer, Individual> individuals;
    private ArrayList<Integer> individualIndexes;

    @Override
    public Iterator<Individual> iterator() {
        return ((ArrayList<Integer>) individualIndexes.clone()).stream().map(index -> individuals.get(index)).iterator();
    }

    public static class IndividualComparator implements Comparator<Individual> {

        @Override
        public int compare(Individual o1, Individual o2) {
                return o2.id - o1.id;
        }
    }
    public Clan(Individual founder) {
        id = founder.clan;
        individuals = new HashMap<>();
        individualIndexes = new ArrayList<>();
        size = 1;
        living = 1;
        individuals.put(founder.id, founder);
        individualIndexes.add(founder.id);
    }
    public void add(Individual i) {
        //System.out.println("before: " + individuals.size());
        individuals.put(i.id, i);
        individualIndexes.add(i.id);
        size++;
        living++;
        //System.out.println("after: " + individuals.size());
        //System.out.println(i.id);
    }
    public void remove(Individual i) {
        individuals.remove(i.id);
        living -= 1;
    }
    public double totalUtility() {
        return individualIndexes.stream().mapToDouble(index -> individuals.get(index).goodTotals()).sum();
    }
    public int size() {
        return size;
    }
    public int living() {
        return living;
    }
    public int[] totalGoods() {
        return new int[]{individualIndexes.stream().mapToInt(ind -> individuals.get(ind).goods[0]).sum(), individualIndexes.stream().mapToInt(ind -> individuals.get(ind).goods[0]).sum()};
    }
    public Individual get(Integer n) {
        return individuals.get(n);
    }
    public Individual getOne(Random r) {
        return individuals.get(individualIndexes.get(r.nextInt(individualIndexes.size())));
    }
    public boolean hasGoods() {
        return individualIndexes.stream().anyMatch(index -> individuals.get(index).hasGoods());
    }
    public void resetIndexes() {
        individualIndexes = new ArrayList<>(individuals.keySet());
    }
    public void removeIndex(Integer index) {
        individualIndexes.remove(index);
    }
}
