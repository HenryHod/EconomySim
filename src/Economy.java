import java.sql.Array;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Economy {
    private int totalGoods;
    private Statement statement;
    private int periodCount;
    private final double meanAltruism;
    private final double meanPatience;
    private final double meanCharity;
    private final double std;
    public final int maxAge;
    public double r;
    private int simId;
    public Random random;
    private HashMap<Integer, Family> families;
    private ArrayList<Integer> familyIndexes;
    public final int childCost  = 20;
    public Economy(int size, Random rand, Statement stmt,double altruism, double patience, double charity, double sd, int age, int id) {
        random = rand;
        families = new HashMap<>();
        familyIndexes = new ArrayList<>();
        totalGoods = 0;
        periodCount = 0;
        meanAltruism = altruism;
        meanPatience = patience;
        meanCharity = charity;
        std = sd;
        maxAge = age;
        simId = id;
        statement = stmt;
        for (int i = 0; i < size; i++) {
            double goods = (double) childCost * rand.nextInt(1, 11);
            families.put(i, new Family(new Individual(rand, i, goods, 0, altruism, patience, charity, sd)));
            familyIndexes.add(i);
            totalGoods += goods;
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
        totalGoods = 0;
        periodCount++;
        resetIndexes();
        r = random.nextDouble(0.5);
        StringBuilder dataString = new StringBuilder("""
                INSERT INTO simulations (sim_id,
                                        period,
                                        mean_altruism,
                                        mean_patience,
                                        mean_charity,
                                        std,
                                        goods,
                                        prev_children,
                                        clan,
                                        family,
                                        generation,
                                        age,
                                        children,
                                        altruism,
                                        patience,
                                        charity,
                                        future_goods,
                                        self_goods,
                                        char_goods,
                                        pref,
                                        utility)
                VALUES""");
        int dsInitLength = dataString.length();
        //int percent = 0;
        while (familyIndexes.size() > 0) {
            //System.out.println(familyIndexes.size());
            /*
            int currentPercent = 100 - (int) (((double) familyIndexes.size() / families.size()) * 100);
            if (currentPercent % 100 == 0 && currentPercent != percent) {
                percent = 100 - (int) (((double) familyIndexes.size() / families.size()) * 100);
                System.out.println("Period " + periodCount + " " + percent + "% Completed");
            }
             */
            Family family = families.get(familyIndexes.get(random.nextInt(familyIndexes.size())));
            Clan clan = family.getOne(random);
            Individual familyMember = clan.getOne(random);
            if (familyMember.startedPeriod()) {
                familyMember.updateData("(" +
                                simId + ", " +
                                periodCount + ", " +
                                meanAltruism + ", " +
                                meanPatience + ", " +
                                meanCharity + ", " +
                                std + ", " +
                                familyMember.goods + ", " +
                                familyMember.children.size() + ", ");
            }
            familyMember.individualTurn(this, family, clan);
            totalGoods += 1;
            //System.out.println("Previous Goods: " + previousGoods[0] + " " + previousGoods[1] + " Current Goods: " + familyMember.goods[0] + " " + familyMember.goods[1]);
            if (!familyMember.hasGoods()) {
                //System.out.println((end - start)/1000.0);
                familyMember.addPeriod();
                familyMember.updateData(familyMember.dataEntry() + "), ");
                dataString.append(familyMember.getDataString());
                familyMember.resetData();
                familyMember.resetGoods();
                familyMember.resetUtility();
                clan.removeIndex(familyMember.id);
                if (!familyMember.hasGoods() || familyMember.age >= maxAge) {
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
                }
            }
            if (dataString.length() > 100000) {
                statement.executeUpdate(String.valueOf(dataString.substring(0, dataString.length() - 2)));
                dataString = new StringBuilder("""
                        INSERT INTO simulations (sim_id,
                                                period,
                                                mean_altruism,
                                                mean_patience,
                                                mean_charity,
                                                std,
                                                goods,
                                                prev_children,
                                                clan,
                                                family,
                                                generation,
                                                age,
                                                children,
                                                altruism,
                                                patience,
                                                charity,
                                                future_goods,
                                                self_goods,
                                                char_goods,
                                                pref,
                                                utility)
                        VALUES""");


            }

        }
        //System.out.println(dataString.substring(0, dataString.length() - 2));
        if (families.size() > 0 && dataString.length() > dsInitLength) {
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
        System.out.println(totalGoods);
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
