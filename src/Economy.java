import org.json.JSONObject;

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
            int apples = childCost * 2; //rand.nextInt(1, 100);
            int oranges = childCost * 2; //rand.nextInt(1, 100);
            families.put(i, new Family( new Individual(rand, i, apples, oranges, 0)));
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
        return families.get(nextFamily).getOne(random);
    }
    public void period() throws SQLException {
        totalGoods = new int[]{0, 0};
        periodCount++;
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
         */
        for (Integer family: ((ArrayList<Integer>) familyIndexes.clone())) {
            for (Clan clan : families.get(family)) {
                for (Individual familyMember: clan) {
                    if (familyMember.goods[0] + familyMember.goods[1] <= 0 || familyMember.age >= 3) {
                        families.get(family).remove(familyMember);
                        if (families.get(family).living() <= 0) {
                            families.remove(family);
                            familyIndexes.remove(family);
                        }
                    }
                }
            }
        }
        for (Integer family: ((ArrayList<Integer>) familyIndexes.clone())) {
            for (Clan clan : families.get(family)) {
                for (Individual familyMember: clan) {
                    int[] previousGoods = familyMember.goods.clone();
                    int previousChildren = familyMember.children.size();
                    familyMember.individualTurn(this);
                    dataString.append("(").append(periodCount).append(", ")
                            .append(previousGoods[0]).append(", ")
                            .append(previousGoods[1]).append(", ")
                            .append(familyMember.children.size() - previousChildren).append(", ")
                            .append(familyMember.dataEntry()).append("), ");
                    //System.out.println((end - start)/1000.0);
                    familyMember.addPeriod();
                    totalGoods[0] += familyMember.goods[0];
                    totalGoods[1] += familyMember.goods[1];
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
                                                        good1_pref,
                                                        good2_pref,
                                                        utility)
                                VALUES""");
                    }
                }
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
        return families.size();
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
}
