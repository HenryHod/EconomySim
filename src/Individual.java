import java.util.*;

public class Individual implements Comparable<Individual>, Iterable<Integer>{
    double currentUtility;
    double currentSelfUtility;
    int birth;
    int age;
    final int id;
    Integer family;
    Integer clan;
    double goods;
    int goodsSelf;
    int goodsFuture;
    int goodsCharity;
    double altruism;
    double charity;
    double patience;
    double preference;
    Individual parent;
    //HashSet<Individual> siblings;
    HashSet<Integer> children;
    StringBuilder dataString;

    public Individual(Random rand, Integer familyNumber, double g, int i,double a, double p, double c,  double sd) {
        family = familyNumber;
        clan = 0;
        age = 0;
        birth = 0;
        id = i;
        altruism = Math.min(Math.max(rand.nextGaussian(a, sd), 0.0), 0.99);
        charity = Math.min(Math.max(rand.nextGaussian(c, sd), 0.0), 0.99);
        patience = Math.min(Math.max(rand.nextGaussian(p, sd), 0.0), 0.99);
        preference = rand.nextDouble(1.0);
        goods = g;
        goodsSelf = 0;
        goodsFuture = 0;
        children = new HashSet<>();
        dataString = new StringBuilder();
        //siblings = new HashSet<>();
    }
    public Individual(Random rand, Integer familyNumber, Individual p,double g, int i, int period) {
        family = familyNumber;
        parent = p;
        clan = p.clan;
        age = 0;
        birth = period;
        id = i;
        altruism = Math.max(Math.min(rand.nextGaussian(p.altruism, 0.05), 0.99), 0);
        charity = Math.max(Math.min(rand.nextGaussian(p.charity, 0.05), 0.99), 0);
        patience = Math.max(Math.min(rand.nextGaussian(p.patience, 0.05), 0.99), 0);
        preference = rand.nextDouble(1.0);
        goods = g;
        goodsSelf = 0;
        goodsFuture = 0;
        children = new HashSet<>();
        dataString = new StringBuilder();
        //siblings = new HashSet<>(p.children);
    }
    public void addGoods(double g) {
        goods += g;
        currentUtility += utilityDiff(g);
    }
    public void print() {
        System.out.println("altruism: " + altruism);
        System.out.println("charity: " + charity);
        System.out.println("preference: " + preference);

    }
    /*
    public double utility() {
        double utility = 0.0;
        utility += utilitySelf(goodsSelf[0], goodsSelf[1]);
        for(Individual child: children) {
            utility += altruism * child.utilitySelf();
            for(Individual grandchild: child.children) {
                utility += altruism * grandchild.utilitySelf();
            }
        }
        if (parent != null) {
            utility += altruism * parent.utilitySelf();
            if (parent.parent != null) {
                utility += parent.parent.utilitySelf();
            }
        }
        for (Individual familyMember: goodsFamily.keySet()) {
            utility += altruism * familyMember.utility(goodsFamily.get(familyMember));
        }
        for (Individual charityCase: goodsCharity.keySet()) {
            utility += charity * charityCase.utility(goodsCharity.get(charityCase));
        }
        return utility;
    }
    */
    /*
    private double utility(int[] receivedGoods) {
        double utility = utility();
        utility -= utilitySelf(goodsSelf[0], goodsSelf[1]);
        utility += utilitySelf(goodsSelf[0] + receivedGoods[0], goodsSelf[1] + receivedGoods[1]);
        return utility;
    }
    */
    public boolean related(Individual other) {
        return Objects.equals(this.family, other.family);
    }
    private void bestDecision(Economy economy, double good, Individual familyMember, Individual charityCase) {
        double utility = utilityDiff(good);
        //- utilityFutDiff(good1, good2);
        String decision = "Self";
        //System.out.println(charityCase);
        //System.out.println("self utility: " + utility);
        //System.out.println("future consumption: " + goodsFuture[0] + " " + goodsFuture[1]);
        //System.out.println("charity: " + charity * charityCase.utilityDiff(good1, good2));
        //System.out.println("Family: " + altruism * familyMember.utilityDiff(good1, good2));
        //System.out.println(patience * utilityFutDiff(good) + " " + utilityDiff(good) + " " + goodsSelf + " " + goodsFuture);
        if (patience * utilityFutDiff(good + economy.r) > utility) {
            //System.out.println("Production");
            decision = "Production";
            good = good + economy.r;
            utility += patience * utilityFutDiff(good);
        }
        if (familyMember != null) {
            if (altruism * familyMember.utilityDiff(good) > utility) {
                //System.out.println("Family");
                decision = "Family";
                utility += altruism * familyMember.utilityDiff(good);
            }
        }
        if (charityCase != null) {
            if (charity * charityCase.utilityDiff(good) > utility) {
                //System.out.println("Charity");
                decision = "Charity";
            }
        }
        executeDecision(economy, decision, good, familyMember, charityCase, utility);
    }
    private void executeDecision(Economy economy, String decision, double good, Individual familyMember, Individual charityCase, Double utility) {
        if (Objects.equals(decision, "Self")) {
            goodsSelf += good;
            currentSelfUtility += utility;
            currentUtility += utility;
        } else if (Objects.equals(decision, "Production")) {
            goodsFuture += good;
        } else if (Objects.equals(decision, "Family")) {
            currentUtility += altruism * utility;
            familyMember.addGoods(good);
            familyMember.currentUtility += utility;
        } else if (Objects.equals(decision, "Charity")) {
            currentUtility = charity * utility;
            goodsCharity += good;
            charityCase.addGoods(good);
            charityCase.currentUtility += utility;
        }
    }
    public void individualTurn(Economy economy, Family f, Clan c) {
        Individual familyMember = null;
        Individual charityCase = null;
        currentSelfUtility = 0;
        currentUtility = 0;
        if (c.living() > 1) {
            familyMember = f.getOne(economy.random).getOne(economy.random);
        }
        if (economy.size() > 1) {
            charityCase = economy.getOne(this);
        }
        //System.out.println(goodsSelf[0] + " " + goodsSelf[1]);
        //System.out.println((1 + (skills / patience )) * goodsSelf[0] + " > or < " + ((skills / patience) * (goods[0]) + goodsFuture[0]));
        //System.out.println((skills + 1) * goodsSelf[1] + " > or < " + (skills * (goods[1]) + goodsFuture[1]));
        //System.out.println(goods[0] + " " + goods[1]);
        //System.out.println("child utility: " + altruism * basicUtility(economy.childCost) + "self utility: " + utilityDiff(economy.childCost));
        if ((goods > economy.childCost &&
                (altruism * basicUtility(economy.childCost) > utilityDiff(economy.childCost))) &&
                (age <= economy.maxAge - 1 && age > 1)) {
            //System.out.println("child utility: " + altruism * basicUtility(economy.childCost) + "self for same goods " + utilityDiff(economy.childCost));
            //System.out.println("family size: " + economy.get(family).size());
            //System.out.println("Before: " + economy.get(family).size());
            Individual child = new Individual(economy.random, family,this, economy.childCost, economy.get(family).size() + 1, economy.currentPeriod());
            addChild(child);
            economy.add(family, child);
            //System.out.println("After: " + economy.get(family).size());
            goods -= economy.childCost;
            currentUtility += altruism * child.potentialUtility();


        } else if (goods >= 1 ) {
                bestDecision(economy, 1.0, familyMember, charityCase);
                goods -=1;

        }
        //System.out.println(apples + " " + oranges + " " + utilityChildDiff() + " " + utilityDiff(5, 5));
    }
    private double utilitySelf(int g) {
        return Math.pow(goodsSelf + g, 1 - preference) / (1 - preference);
    }
    private double utilitySelf() {
            return Math.pow(goodsSelf, 1 - preference) / (1 - preference);
    }
    private double utilityDiff(double g) {
        //System.out.println(utilitySelf(good1, good2) + " " + utilitySelf());
        return Math.pow(goodsSelf + g, 1 - preference) / (1 - preference) - Math.pow(goodsSelf, 1 - preference) / (1 - preference);
    }
    private double utilityFutDiff(double g) {
        return Math.pow(goodsFuture + g, 1 - preference) / (1 - preference) - Math.pow(goodsFuture, 1 - preference) / (1 - preference);
    }
    private double childUtility(double g, Random rand) {
        double pref = Math.min(Math.max(rand.nextGaussian(preference, 0.05), 0.0), 0.99);
        return Math.pow(g, 1 - pref) / (1 - pref);
    }
    public double getCurrentUtility() {
        return currentUtility;
    }
    private void addChild(Individual child) {
        children.add(child.id);
        //System.out.println(children.size());
    }
    /*
    private boolean consumptionCheck() {
        double denom = 0.0;
        for (int i = 0; i < 5 - age; i++) {
            denom += Math.pow(patience / skills, i);
        }
        if (children.size() > 0) {
            for (Integer child: children) {
                for (int i = 0; i < 5 - age; i++) {
                    denom += altruism * Math.pow(child.patience / child.skills, i);
                }
            }
        }
        return goodsSelf[0] > goods[0] / denom | goodsSelf[1] > goods[0] / denom;
    }

     */
    public void addPeriod() {
        age++;
    }
    public double goodTotals() {
        return goods;
    }
    public double potentialUtility() {
        return Math.pow(goods, 1 - preference) / (1 - preference);
    }
    public double basicUtility(double g) {
        return Math.pow(g, 1 - preference) / (1 - preference);
    }
    public String dataEntry() {
        return "" + clan
                + ", " + family
                + ", " + id
                + ", " + age
                + ", " + children.size()
                + ", " + altruism
                + ", " + patience
                + ", " + charity
                + ", " + goodsFuture
                + ", " + goodsSelf
                + ", " + goodsCharity
                + ", " + preference
                + ", " + currentUtility;
    }
    @Override
    public int compareTo(Individual o) {
        return (int) (currentUtility - o.currentUtility);
    }
    public boolean isSibling(Individual i) {
        if (this != i) {
            return parent == i.parent;
        }
        return false;
    }
    public void inheritance(Individual child) {
        child.addGoods(goods / children.size());
    }
    public void resetParent() {
        this.parent = null;
    }
    public void deleteChild(Integer childIndex) {
        children.remove(childIndex);
    }
    public boolean hasParent() {
        return parent != null;
    }
    public boolean hasGoods() { return goods >= 1.0;}
    public void resetGoods() {
        goods = goodsFuture;
        goodsFuture = 0;
        goodsSelf = 0;
        goodsCharity = 0;
    }
    public void resetData() {
        dataString = new StringBuilder();
    }
    public void resetUtility() {
        currentUtility = 0;
        currentSelfUtility = 0;
    }
    public boolean startedPeriod() {
        return dataString.length() == 0;
    }
    public void updateData(String str) {
        dataString.append(str);
    }
    public StringBuilder getDataString() {
        return dataString;
    }
    @Override
    public Iterator<Integer> iterator() {
        return children.iterator();
    }
}
