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
    public Economy(int size, Random rand, Statement stmt) {
        random = rand;
        families = new HashMap<>();
        totalGoods = new int[]{0, 0};
        periodCount = 0;
        statement = stmt;

        for (int i = 0; i < size; i++) {
            int apples = 20; //rand.nextInt(1, 100);
            int oranges = 20; //rand.nextInt(1, 100);
            families.put(i, new Family( new Individual(rand, i, apples, oranges, 0)));
            totalGoods[0] += apples;
            totalGoods[1] += oranges;
        }
    }
    public Individual findLeastUtils(Individual self) {
        PriorityQueue<Individual> leastUtilsIndividual = new PriorityQueue<>();
        int familyCount = 0;
        for (Integer family: families.keySet()) {
            if (!Objects.equals(family, self.family)) {
                leastUtilsIndividual.add(families.get(family).leastUtils());
                familyCount++;
            }
            if (familyCount >= 5) {
                break;
            }
        }
        return leastUtilsIndividual.poll();
    }
    public void period() throws SQLException {
        totalGoods = new int[]{0, 0};
        periodCount++;
        StringBuilder dataString = new StringBuilder("""
                INSERT INTO simulations (period,
                                        family,
                                        generation,
                                        age,
                                        children,
                                        altruism,
                                        impatience,
                                        charity,
                                        skills,
                                        good1,
                                        good2,
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
        for (Integer family: new HashSet<>(families.keySet())) {
            for (Individual familyMember : families.get(family)) {
                if (familyMember.goods[0] + familyMember.goods[1] > 0 && familyMember.age < 3) {
                    familyMember.individualTurn(this);
                    dataString.append("(").append(periodCount).append(", ").append(familyMember.dataEntry()).append("), ");
                    //System.out.println((end - start)/1000.0);
                    familyMember.addPeriod();
                    totalGoods[0] += familyMember.goods[0];
                    totalGoods[1] += familyMember.goods[1];
                    if (dataString.length() > 100000) {
                        statement.executeUpdate(String.valueOf(dataString.substring(0, dataString.length() - 2)));
                        dataString = new StringBuilder("""
                                INSERT INTO simulations (period,
                                                        family,
                                                        generation,
                                                        age,
                                                        children,
                                                        altruism,
                                                        impatience,
                                                        charity,
                                                        skills,
                                                        good1,
                                                        good2,
                                                        good1_pref,
                                                        good2_pref,
                                                        utility)
                                VALUES""");
                    }
                } else {
                    families.get(family).remove(familyMember);
                    if (families.get(family).living() <= 0) {
                        families.remove(family);
                    }
                }
            }

        }
        //System.out.println(dataString.substring(0, dataString.length() - 2));
        if (families.size() > 0) {
            statement.executeUpdate(String.valueOf(dataString.substring(0, dataString.length() - 2)));
        }
    }
    public void add(Integer family,Individual i) {
        families.get(family).add(i);
    }
    public Family get(Integer i) {
        return families.get(i);
    }
    public int size() {
        return families.size();
    }
    public void print() {
        System.out.println("Period: " + periodCount);
        System.out.println(families.keySet().stream().mapToInt(key -> families.get(key).size()).sum());
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
