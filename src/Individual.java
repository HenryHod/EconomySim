import java.util.*;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

public class Individual implements Comparable<Individual>{
    ChiSquaredDistribution childrenUtility = new ChiSquaredDistribution(5);
    double currentUtility;
    int age;
    Integer family;
    int[] goods;
    int[] goodsSelf;
    int[] goodsFuture;
    HashMap<Individual, int[]> goodsFamily;
    HashMap<Individual, int[]> goodsCharity;
    int skills;
    double altruism;
    double charity;
    double impatience;
    double[] preferences;
    Individual parent;
    HashSet<Individual> siblings;
    HashSet<Individual> children;

    public Individual(Random rand, Integer familyNumber, int good1, int good2) {
        family = familyNumber;
        age = 0;
        skills = rand.nextInt(5,10);
        altruism = Math.min(rand.nextGaussian(0.95, 0.05), 1);
        charity = rand.nextDouble(1.0);
        impatience = Math.min(rand.nextGaussian(0.85, 0.05), 1);
        double applePreference = rand.nextDouble(1.0);
        preferences = new double[]{applePreference, 1 - applePreference};
        goods = new int[]{good1, good2};
        goodsSelf = new int[]{0, 0};
        goodsFuture = new int[]{0, 0};
        goodsFamily = new HashMap<>();
        goodsCharity = new HashMap<>();
        children = new HashSet<>();
    }
    public Individual(Random rand, Integer familyNumber, Individual p, int good1, int good2) {
        family = familyNumber;
        parent = p;
        age = 0;
        skills = rand.nextInt(Math.max(parent.skills - 5, 1), Math.min(parent.skills + 5, 10));
        altruism = Math.min(rand.nextGaussian(parent.altruism, 0.05), 1);
        charity = rand.nextDouble(Math.max(parent.charity - 0.2, 0.0), Math.min(parent.charity + 0.2, 1.0));
        impatience = Math.min(rand.nextGaussian(parent.impatience, 0.05), 1);
        double applePreference = rand.nextDouble(1.0);
        preferences = new double[]{applePreference, 1 - applePreference};
        goods = new int[]{good1, good2};
        goodsSelf = new int[]{0, 0};
        goodsFuture = new int[]{0, 0};
        goodsFamily = new HashMap<>();
        goodsCharity = new HashMap<>();
        children = new HashSet<>();
    }
    public void addGoods(int apples, int oranges) {
        goodsSelf[0] += apples;
        goodsSelf[1] += oranges;
        currentUtility += utilityDiff(apples, oranges);


    }
    public void print() {
        System.out.println("skills: " + skills);
        System.out.println("altruism: " + altruism);
        System.out.println("charity: " + charity);
        System.out.println("preferences: " + preferences[0] + " " + preferences[1]);

    }
    private double utility() {
        double utility = 0.0;
        utility += utilitySelf(goodsSelf[0], goodsSelf[1]);
        for(Individual child: children.stream().filter(child -> !goodsFamily.containsKey(child)).toList()) {
            utility += altruism * child.utility();
            for(Individual grandchild: child.children) {
                utility += altruism * grandchild.utility();
            }
        }
        if (parent != null) {
            utility += altruism * parent.utility();
            if (parent.parent != null) {
                utility += parent.parent.utility();
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
    private double utility(int[] receivedGoods) {
        double utility = utility();
        utility -= utilitySelf(goodsSelf[0], goodsSelf[1]);
        utility += utilitySelf(goodsSelf[0] + receivedGoods[0], goodsSelf[1] + receivedGoods[1]);
        return utility;
    }
    public boolean related(Individual other) {
        return Objects.equals(this.family, other.family);
    }
    private void bestDecision(Economy economy, int good1, int good2) {
        double utility = utilityDiff(good1, good2);
        //- utilityFutDiff(good1, good2);
        String decision = "Self";
        Individual familyMember = this;
        Individual charityCase = this;
        //System.out.println(charityCase);
        if (economy.get(family).size() > 1) {
            familyMember = economy.get(family).leastUtils();
        }
        if (economy.size() > 1) {
            charityCase = economy.findLeastUtils(this);
        }
        System.out.println("self utility: " + utility);
        System.out.println("future consumption: " + goodsFuture[0] + " " + goodsFuture[1]);
        System.out.println("charity: " + charity * charityCase.utilityDiff(good1, good2));
        System.out.println("Family: " + altruism * familyMember.utilityDiff(good1, good2));
        if ((1 + (skills / impatience )) * goodsSelf[0] > skills * goods[0] + goodsFuture[0] |  (skills + 1) * goodsSelf[1] > skills * goods[1] + goodsFuture[1] ) {
            //System.out.println("Production");
            decision = "Production";
        }
        if (altruism * familyMember.utilityDiff(good1, good2) > utility) {
            //System.out.println("Family");
            decision = "Family";
        }
        if (charity * charityCase.utilityDiff(good1, good2) > utility) {
            //System.out.println("Charity");
            decision = "Charity";
        }
        executeDecision(economy, decision, good1, good2, familyMember, charityCase);
    }
    private void executeDecision(Economy economy, String decision, int good1, int good2, Individual familyMember, Individual charityCase) {
        double utility = utilityDiff(good1, good2);
        if (Objects.equals(decision, "Self")) {
            goodsSelf[0] += good1;
            goodsSelf[1] += good2;
        } else if (Objects.equals(decision, "Production")) {
            goodsFuture[0] += good1 * economy.random.nextInt(Math.max(0, skills - 2), Math.min(skills + 2, 10));
            goodsFuture[1] += good2 * economy.random.nextInt(Math.max(0, skills - 2), Math.min(skills + 2, 10));
        } else if (Objects.equals(decision, "Family")) {
            utility = altruism * familyMember.utilityDiff(good1, good2);
            familyMember.addGoods(good1, good2);
            familyMember.currentUtility += familyMember.utilityDiff(good1, good2);
        } else if (Objects.equals(decision, "Charity")) {
            utility = charity * charityCase.utilityDiff(good1, good2);
            charityCase.addGoods(good1, good2);
            charityCase.currentUtility += charityCase.utilityDiff(good1, good2);
        }
        currentUtility += utility;
    }
    public void individualTurn(Economy economy) {
        currentUtility = 0;
        goodsSelf = new int[]{0, 0};
        goodsFuture = new int[]{0, 0};
        int apples = goods[0];
        int oranges = goods[1];
        //System.out.println(goodsSelf[0] + " " + goodsSelf[1]);
        while (apples > 0 | oranges > 0) {
            System.out.println((1 + (skills / impatience )) * goodsSelf[0] + " > or < " + ((skills / impatience) * (goods[0]) + goodsFuture[0]));
            System.out.println((skills + 1) * goodsSelf[1] + " > or < " + (skills * (goods[1]) + goodsFuture[1]));
            System.out.println(goods[0] + " " + goods[1]);
            System.out.println("child utility: " + utilityChildDiff() + "self for same goods " + utilityDiff(5, 5) + " for bundle of goods " + goodsSelf[0] + " " +  goodsSelf[1]);
            if ((oranges >= 5 & apples >= 5) & (utilityChildDiff()) > 5 & age >= 2) {
                //System.out.println("child utility: " + utilityChildDiff() + "self for same goods " + utilityDiff(5, 5));
                Individual child = new Individual(economy.random, family,this, 5, 5);
                addChild(child);
                economy.add(family, child);
                apples -= 5;
                oranges -= 5;
                currentUtility += utilityChildDiff();

            }
            if (apples > 0 & oranges > 0) {
                bestDecision(economy, 1, 1);
                apples -= 1;
                oranges -= 1;
            } else if (apples > 0) {
                bestDecision(economy, 1, 0);
                apples -= 1;
            } else {
                bestDecision(economy, 0, 1);
                oranges -= 1;
            }
            //System.out.println(apples + " " + oranges + " " + utilityChildDiff() + " " + utilityDiff(5, 5));

        }
        goods = goodsFuture;
    }
    private double utilitySelf(int good1, int good2) {
        return Math.pow(goodsSelf[0] + good1 + 1, preferences[0]) * Math.pow(goodsSelf[1] + good2 + 1, preferences[1]) - 1;
    }
    private double utilitySelf() {
            return Math.pow(goodsSelf[0] + 1, preferences[0]) * Math.pow(goodsSelf[1] + 1, preferences[1]) - 1;
    }
    private double utilityDiff(int good1, int good2) {
        //System.out.println(utilitySelf(good1, good2) + " " + utilitySelf());
        return (utilitySelf(good1, good2) - utilitySelf());
    }
    private double utilityFutDiff(int good1, int good2) {
        double previous = (Math.pow(goodsFuture[0] + 1, preferences[0]) * Math.pow(goodsFuture[1] + 1, preferences[1]) - 1);
        double current = (Math.pow(goodsFuture[0] + good1 * skills + 2, preferences[0]) * Math.pow(goodsFuture[1] + good2 * skills + 1, preferences[1]) - 1);
        return (current - previous)/skills;
    }
    private double ln(int x) {
        return Math.log(x + 1);
    }
    private double utilityChildDiff() {
        //System.out.println("z value: " + ((double) children.size() + 1.0 - childrenUtility.getNumericalMean())/Math.pow(childrenUtility.getNumericalVariance(), 0.5));
        //System.out.println("Random Chi Values: " + childrenUtility.density(5.0) + " " + childrenUtility.density(3.00));
        //System.out.println("child utility: " + (altruism * 5 + 5 * childrenUtility.density((double) children.size() + 1.0)));
        return altruism  * (ln(children.size() + 1) - ln(children.size()) + 5);
    }
    public double getCurrentUtility() {
        return currentUtility;
    }
    private void addChild(Individual child) {
        this.children.add(child);

    }
    public void removeSelf() {
        if ((goods[0] > 0 | goods[1] > 0) & children.size() > 0) {
            for (Individual child: children) {
                child.addGoods(goods[0] / children.size(), goods[1] / children.size() );
            }
        }
    }
    public void addPeriod() {
        age++;
    }
    @Override
    public int compareTo(Individual o) {
        return (int) (currentUtility - o.currentUtility);
    }
}
