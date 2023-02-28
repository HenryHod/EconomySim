
import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.sum;

public class Family implements Iterable<Clan> {
    private int size;
    public static class IndividualComparator implements Comparator<Individual> {

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
            return Double.compare(o1.getCurrentUtility(), o2.getCurrentUtility());
        }
    }
    private HashMap<Integer, Clan> clans;
    public Family(Individual founder) {
        clans = new HashMap<>();
        size = 1;
        clans.put(0, new Clan(founder));
    }
    public Individual leastUtils(Individual i) {
        return clans.get(i.clan).leastUtils();
    }
    public Individual leastUtils() {
        return clans.keySet().stream().map(key -> clans.get(key).leastUtils()).min(new IndividualComparator()).get();
    }
    public void add(Individual i) {
        //System.out.println("before: " + individuals.size());
        clans.get(i.clan).add(i);
        size++;
        //System.out.println("after: " + individuals.size());
        //System.out.println(i.id);
    }
    public void remove(Individual i) {
        for (Individual child: i) {
            clans.get(child.clan).remove(child);
            child.clan = size;
            clans.put(child.clan, new Clan(child));
            size++;
            for (Individual grandchild: child) {
                clans.get(grandchild.clan).remove(grandchild);
                clans.get(child.clan).add(grandchild);
            }
        }
        clans.get(i.clan).remove(i);
        i.removeSelf();
        //System.out.println(clans.get(i.clan).living());
        if (clans.get(i.clan).living() == 0) {
            System.out.println("Remove Clan: " + i.clan + " From Family: " + i.family + " Last Individual: " + i.id);
            clans.remove(i.clan);
        }
    }
    public double totalUtility() {
        return clans.keySet().stream().mapToDouble(key -> clans.get(key).totalUtility()).sum();
    }
    public int size() {
        return size;
    }
    public int living() {
        return clans.keySet().stream().mapToInt(key -> clans.get(key).living()).sum();
    }
    public int[] totalGoods() {
        int[] total = new int[2];
        for (Clan clan: this) {
            int[] clanTotal = clan.totalGoods();
            total[0] += clanTotal[0];
            total[1] += clanTotal[1];
        }
        return total;
    }
    @Override
    public Iterator<Clan> iterator() {
        HashMap<Integer, Clan> clansCopy = (HashMap<Integer, Clan>) clans.clone();
        return clansCopy.keySet().stream().map(clansCopy::get).iterator();
    }
    public Clan get(Integer clan) {
        return clans.get(clan);
    }
}
