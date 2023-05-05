import java.sql.Array;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Economy {
    public int totalGoods;
    private int population;
    private Statement statement;
    private int periodCount;
    public final int maxAge;
    public final int simId;
    public double r;
    public double meanAltruism;
    public double meanPatience;
    public double meanCharity;
    public double std;
    public Random random;
    private HashMap<Integer, Family> families;
    private ArrayList<Integer> familyIndexes;
    public final int childCost  = 20;
    public Economy(int size, Random rand, Statement stmt, double altruism, double patience, double charity, double sd, int age, int id) {
        random = rand;
        families = new HashMap<>();
        familyIndexes = new ArrayList<>();
        periodCount = 0;
        maxAge = age;
        simId = id;
        meanAltruism = altruism;
        meanPatience = patience;
        meanCharity = charity;
        std = sd;
        statement = stmt;
        population = size;
        for (int i = 0; i < size; i++) {
            double goods = (double) childCost * rand.nextInt(1, 21);
            families.put(i, new Family(new Individual(rand, i, goods, 0, altruism, patience, charity, sd)));
            familyIndexes.add(i);
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
        int initDataLength = dataString.length();

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
                    family.remove(this, familyMember);
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
        if (families.size() > 0 && dataString.length() > initDataLength) {
            statement.executeUpdate(String.valueOf(dataString.substring(0, dataString.length() - 2)));
        }
    }
    public void aggPeriod() throws SQLException {
        int startPopulation = population;
        int totalFutureGoods = 0;
        int totalSelfGoods = 0;
        int totalCharityGoods = 0;
        periodCount++;
        resetIndexes();
        r = random.nextDouble(0.2);
        StringBuilder dataString = new StringBuilder("""
                                    INSERT INTO economies (sim_id,
                                        period,
                                        start_population,
                                        population,
                                        goods,
                                        future_goods,
                                        self_goods,
                                        char_goods,
                                        mean_altruism,
                                        mean_patience,
                                        mean_charity)
                                    VALUES""");
        int initDataLength = dataString.length();

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
            if (!familyMember.startedPeriod()) {
                familyMember.startPeriod();
            }
            familyMember.individualTurn(this, family, clan);
            //System.out.println("Previous Goods: " + previousGoods[0] + " " + previousGoods[1] + " Current Goods: " + familyMember.goods[0] + " " + familyMember.goods[1]);
            if (!familyMember.hasGoods()) {
                //System.out.println((end - start)/1000.0);
                familyMember.addPeriod();
                totalFutureGoods += familyMember.futGoods();
                totalSelfGoods += familyMember.selfGoods();
                totalCharityGoods += familyMember.charGoods();
                familyMember.resetData();
                familyMember.resetGoods();
                familyMember.resetUtility();
                familyMember.endPeriod();
                clan.removeIndex(familyMember.id);
                if (!familyMember.hasGoods() || familyMember.age >= maxAge) {
                    population -= 1;
                    //System.out.println(family + " " + familyMember.clan + " " + familyMember.id + " Deleted");
                    family.remove(this, familyMember);
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
        }
        //System.out.println(dataString.substring(0, dataString.length() - 2));
        if (families.size() > 0) {
            dataString.append("( " +
                            simId + ", " +
                            periodCount + ", " +
                            startPopulation + ", " +
                            population + ", " +
                            ((totalFutureGoods / (1 + r)) + totalSelfGoods + totalCharityGoods) + ", " +
                            (totalFutureGoods / (1 + r)) + ", " +
                            totalSelfGoods + ", " +
                            totalCharityGoods + ", " +
                            meanAltruism + ", " +
                            meanPatience + ", " +
                            meanCharity + ")"
            );
            //System.out.println("Goods for Next Period " + totalFutureGoods);
            statement.executeUpdate(String.valueOf(dataString));
        }

    }
    public void add(Integer family,Individual i) {
        population++;
        families.get(family).add(i);
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
    public double rv() {
        return random.nextGaussian(0, 1);
    }
}
