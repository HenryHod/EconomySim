
import java.lang.reflect.Array;
import java.util.*;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.sum;

public class Family implements Iterable<Clan> {
    private int size;
    private int living;
    private HashMap<Integer, Clan> clans;
    private ArrayList<Integer> clanIndexes;
    public Family(Individual founder) {
        clans = new HashMap<>();
        clanIndexes = new ArrayList<>();
        size = 1;
        living = 1;
        clans.put(0, new Clan(founder));
        clanIndexes.add(founder.clan);
    }
    public Individual leastUtils(Individual i) {
        return clans.get(i.clan).leastUtils();
    }
    public Individual getOne(Random r) {
        return clans.get(clanIndexes.get(r.nextInt(clanIndexes.size()))).leastUtils();
    }
    public void add(Individual i) {
        //System.out.println("before: " + individuals.size());
        clans.get(i.clan).add(i);
        size++;
        living++;
        //System.out.println("after: " + individuals.size());
        //System.out.println(i.id);
    }
    public void remove(Individual i) {
        Clan currentClan = clans.get(i.clan);
        for (Integer childIndex: i) {
            Individual child = currentClan.get(childIndex);
            currentClan.remove(child);
            child.clan = size;
            clans.put(child.clan, new Clan(child));
            clanIndexes.add(child.clan);
            size++;
            for (Integer grandchildIndex: child) {
                Individual grandchild = currentClan.get(grandchildIndex);
                currentClan.remove(grandchild);
                grandchild.clan = child.clan;
                clans.get(child.clan).add(grandchild);
            }
        }
        currentClan.remove(i);
        i.removeSelf(currentClan);
        living -= 1;
        //System.out.println(clans.get(i.clan).living());
        if (clans.get(i.clan).living() == 0) {
            //System.out.println("Remove Clan: " + i.clan + " From Family: " + i.family + " Last Individual: " + i.id);
            clans.remove(i.clan);
            clanIndexes.remove(i.clan);
        }
    }
    public double totalUtility() {
        return clans.keySet().stream().mapToDouble(key -> clans.get(key).totalUtility()).sum();
    }
    public int size() {
        return size;
    }
    public int living() {
        return living;
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
    public Iterator<Clan> noCopyIterator() {
        return clans.keySet().stream().map(clans::get).iterator();
    }
    @Override
    public Iterator<Clan> iterator() {
        return ((ArrayList<Integer>) clanIndexes.clone()).stream().map(clans::get).iterator();
    }
    public Clan get(Integer clan) {
        return clans.get(clan);
    }
}
