import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
public class Individual implements Comparable<Individual>{
    double currentUtility;
    Integer family;
    int[] goods;
    int[] goodsSelf;
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
        skills = rand.nextInt(10);
        altruism = rand.nextDouble(1.0);
        charity = rand.nextDouble(1.0);
        impatience = rand.nextDouble();
        double applePreference = rand.nextDouble(1.0);
        preferences = new double[]{applePreference, 1 - applePreference};
        goods = new int[]{good1, good2};
        goodsSelf = new int[]{good1, good2};
        goodsFamily = new HashMap<>();
        goodsCharity = new HashMap<>();

    }
    public Individual(Random rand, Integer familyNumber, Individual p, int good1, int good2) {
        family = familyNumber;
        parent = p;
        skills = rand.nextInt(Math.max(parent.skills - 5, 0), Math.min(parent.skills + 5, 10));
        altruism = rand.nextDouble(Math.max(parent.altruism - 0.3, 0.0), Math.min(parent.altruism + 0.3, 1.0));
        charity = rand.nextDouble(Math.max(parent.charity - 0.4, 0.0), Math.min(parent.charity + 0.4, 1.0));
        impatience = rand.nextDouble();

        double applePreference = rand.nextDouble(1.0);
        preferences = new double[]{applePreference, 1 - applePreference};
        goods = new int[]{good1, good2};
        goodsSelf = new int[]{0, 0};
        goodsFamily = new HashMap<>();
        goodsCharity = new HashMap<>();
    }
    public void changeGoods(int apples, int oranges) {
        goods[0] += apples;
        goods[1] += oranges;

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
        Individual receiver = this;
        double utility = utilityDiff(good1, good2);
        Individual familyMember = economy.get(family).leastUtils();
        Individual charityCase = economy.findLeastUtils(this);
        if (altruism * familyMember.utilityDiff(good1, good2) > utility) {
            receiver = familyMember;
            utility = altruism * familyMember.utilityDiff(good1, good2);
        }
        if (charity * charityCase.utilityDiff(good1, good2) > utility) {
            receiver = charityCase;
            utility = charity * charityCase.utilityDiff(good1, good2);
        }
        if ((1/(1 + impatience) * (1/(double) skills)) * utilityDiff(good1 * skills, good2 * skills) > utility) {
            good1 = good1 * skills;
            good2 = good2 * skills;
        }
        receiver.changeGoods(good1, good2);
        receiver.currentUtility += receiver.utilityDiff(good1, good2);
        currentUtility += utility;
    }
    public void individualTurn(Economy economy) {
        currentUtility = 0;
        goodsSelf = new int[]{0, 0};
        int apples = goods[0];
        int oranges = goods[1];
        while (apples > 0 | oranges > 0) {
            if (apples > 0) {
                bestDecision(economy, 1, 0);
                apples -= 1;
                goods[0] -= 1;
            }
            if (oranges > 0) {
                bestDecision(economy, 0, 1);
                oranges -= 1;
                goods[1] -= 1;
            }
            if ((oranges >= 5) & (apples >= 5) & (utilityChildDiff()) > utilityDiff(5, 5)) {
                
            }
        }
    }
    private double ln(int x) {
        return Math.log(x + 1);
    }
    private double ln(double x) {
        return Math.log(x + 1);
    }
    private double utilitySelf(int good1, int good2) {
        return preferences[0] * ln(goodsSelf[0] + good1) + preferences[1] * ln(goodsSelf[1] + good2);
    }
    private double utilitySelf() {
        return preferences[0] * ln(goodsSelf[0]) + preferences[1] * ln(goodsSelf[1]);
    }
    private double utilityDiff(int good1, int good2) {
        return utilitySelf(good1, good2) - utilitySelf();
    }
    private double utilityChildDiff() {
        return altruism * Math.log(children.size() + 2) - altruism * Math.log(children.size() + 1);
    }
    public double getCurrentUtility() {
        return currentUtility;
    }
    @Override
    public int compareTo(Individual o) {
        return 0;
    }
}
