import java.sql.Array;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Economy {
    private int[] totalGoods;
    private Statement statement;
    private int periodCount;
    public Random random;
    private HashMap<Integer, Family> families;
    private ArrayList<Integer> familyIndexes;
    public final int childCost  = 20;
    public Economy(int size, Random rand, Statement stmt) {
        random = rand;
        families = new HashMap<>();
        familyIndexes = new ArrayList<>();
        totalGoods = new int[]{0, 0};
        periodCount = 0;
        statement = stmt;
        for (int i = 0; i < size; i++) {
            int apples = (childCost / 2) * rand.nextInt(1, 21);
            int oranges = (childCost / 2) * rand.nextInt(1, 21);
            families.put(i, new Family(new Individual(rand, i, apples, oranges, 0)));
            familyIndexes.add(i);
            totalGoods[0] += apples;
            totalGoods[1] += oranges;
        }
    }
    public Individual getOne(Individual self) {
        Integer nextFamily = familyIndexes.get(random.nextInt(familyIndexes.size()));
        while (Objects.equals(nextFamily, self.family)) {
            nextFamily = familyIndexes.get(random.nextInt(familyIndexes.size()));
        }
        return families.get(nextFamily).getOne(random).getOne(random);
    }
    public void period() throws SQLException {
        totalGoods = new int[]{0, 0};
        periodCount++;
        resetIndexes();
        StringBuilder dataString = new StringBuilder("""
                INSERT INTO simulations (period,
                                        good1,
                                        good2,
                                        new_children,
                                        clan,
                                        family,
                                        generation,
                                        age,
                                        children,
                                        altruism,
                                        impatience,
                                        charity,
                                        skills,
                                        future_good1,
                                        future_good2,
                                        self_good1,
                                        self_good2,
                                        good1_pref,
                                        good2_pref,
                                        utility)
                VALUES""");
        /*
        for (Integer family: new HashSet<>(families.keySet())) {
            for (Individual familyMember : families.get(family)) {
                if (((familyMember.goods[0] + familyMember.goods[1]) == 0) || (familyMember.age >= 6)) {
                    families.get(family).remove(familyMember);
                    if (families.get(family).size() <= 0) {
                        families.remove(family);
                    }
                }
            }
        }
        for (Integer familyID: ((ArrayList<Integer>) familyIndexes.clone())) {
            Family family = families.get(familyID);
            for (Integer clanID : family) {
                if (family.contains(clanID)) {
                    Clan clan = family.get(clanID);
                    for (Integer id: clan) {
                        if (clan.contains(id)) {
                            Individual familyMember = clan.get(id);
                            //System.out.println("Family " + familyID + " Clan " + clanID + " ID " + id);
                            if (!familyMember.hasGoods() || familyMember.age >= 3) {
                                //System.out.println(family + " " + familyMember.clan + " " + familyMember.id + " Deleted");
                                family.remove(familyMember);
                                //System.out.println(family + " " + familyMember.clan + " Deleted");
                                if (family.living() <= 0) {
                                    //System.out.println(family + " Deleted");
                                    families.remove(familyID);
                                    familyIndexes.remove(familyID);
                                    removeIndex(familyID);
                                }
                            }
                        }
                    }
                }
            }
        }
         */
        while (familyIndexes.size() > 0) {
            //System.out.println(familyIndexes.size());
            Family family = families.get(familyIndexes.get(random.nextInt(familyIndexes.size())));
            Clan clan = family.getOne(random);
            Individual familyMember = clan.getOne(random);
            int[] previousGoods = familyMember.goods.clone();
            int previousChildren = familyMember.children.size();
            familyMember.individualTurn(this);
            //System.out.println("Previous Goods: " + previousGoods[0] + " " + previousGoods[1] + " Current Goods: " + familyMember.goods[0] + " " + familyMember.goods[1]);
            if (!familyMember.hasGoods()) {
                dataString.append("(").append(periodCount).append(", ")
                        .append(previousGoods[0]).append(", ")
                        .append(previousGoods[1]).append(", ")
                        .append(familyMember.children.size() - previousChildren).append(", ")
                        .append(familyMember.dataEntry()).append("), ");
                //System.out.println((end - start)/1000.0);
                familyMember.addPeriod();
                familyMember.setFutureGoods();
                clan.removeIndex(familyMember.id);
                if (!familyMember.hasGoods() || familyMember.age >= 3) {
                    //System.out.println(family + " " + familyMember.clan + " " + familyMember.id + " Deleted");
                    family.remove(familyMember);
                    //System.out.println(family + " " + familyMember.clan + " Deleted");
                    if (family.living() <= 0) {
                        //System.out.println(family + " Deleted");
                        families.remove(familyMember.family);
                        familyIndexes.remove(familyMember.family);
                        removeIndex(familyMember.family);
                    }
                }
                if (!clan.hasGoods()) {
                    family.removeIndex(familyMember.clan);
                    if (!family.hasGoods()) {
                        removeIndex(familyMember.family);
                    }
                };
            }
            if (dataString.length() > 100000) {
                statement.executeUpdate(String.valueOf(dataString.substring(0, dataString.length() - 2)));
                dataString = new StringBuilder("""
                        INSERT INTO simulations (period,
                                                good1,
                                                good2,
                                                new_children,
                                                clan,
                                                family,
                                                generation,
                                                age,
                                                children,
                                                altruism,
                                                impatience,
                                                charity,
                                                skills,
                                                future_good1,
                                                future_good2,
                                                self_good1,
                                                self_good2,
                                                good1_pref,
                                                good2_pref,
                                                utility)
                        VALUES""");


            }

        }
        //System.out.println(dataString.substring(0, dataString.length() - 2));
        if (families.size() > 0) {
            statement.executeUpdate(String.valueOf(dataString.substring(0, dataString.length() - 2)));
        }
    }
    public void add(Integer family,Individual i) {families.get(family).add(i);
    }
    public Family get(Integer i) {
        return families.get(i);
    }
    public int size() {
        return familyIndexes.size();
    }
    public void print() {
        System.out.println("Period: " + periodCount);
        System.out.println(families.keySet().stream().mapToInt(key -> families.get(key).living()).sum());
        System.out.println(totalGoods[0] + " " + totalGoods[1]);
        System.out.println(families.keySet().stream().mapToDouble(key -> families.get(key).totalUtility()).sum());
    }
    private void recordData(Individual i) throws SQLException {
        statement.executeUpdate("""
                                INSERT INTO simulations (period,
                                                        family,
                                                        generation,
                                                        children,
                                                        altruism,
                                                        charity,
                                                        good1,
                                                        good2,
                                                        good1_pref,
                                                        good2_pref,
                                                        utility)
                                VALUES (
                                """
                                + periodCount + ", "
                                + i.dataEntry() + ")"
                                );
    }
    public int currentPeriod() {
        return periodCount;
    }
    public void resetIndexes() {
        familyIndexes = new ArrayList<>(families.keySet());
        for (Integer familyIndex: familyIndexes) {
            families.get(familyIndex).resetIndexes();
        }
    }
    public void removeIndex(Integer index) {
        familyIndexes.remove(index);
    }
}
