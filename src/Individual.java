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
    double[] preferences;
    Individual parent;
    HashSet<Individual> siblings;
    HashSet<Individual> children;

    public Individual(Random rand, Integer familyNumber) {
        family = familyNumber;
        skills = rand.nextInt(10);
        altruism = rand.nextDouble(1.0);
        charity = rand.nextDouble(1.0);
        double applePreference = rand.nextDouble(1.0);
        preferences = new double[]{applePreference, 1 - applePreference};
        goods = new int[]{0, 0};
    }
    public Individual(Random rand, Integer familyNumber, Individual p) {
        family = familyNumber;
        parent = p;
        skills = rand.nextInt(Math.max(parent.skills - 5, 0), Math.min(parent.skills + 5, 10));
        altruism = rand.nextDouble(Math.max(parent.altruism - 0.3, 0.0), Math.min(parent.altruism + 0.3, 1.0));
        charity = rand.nextDouble(Math.max(parent.charity - 0.4, 0.0), Math.min(parent.charity + 0.4, 1.0));
        double applePreference = rand.nextDouble(1.0);
        preferences = new double[]{applePreference, 1 - applePreference};
        goods = new int[]{0, 0};
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
    public double utility() {
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
    public double utility(int[] receivedGoods) {
        double utility = utility();
        utility -= utilitySelf(goodsSelf[0], goodsSelf[1]);
        utility += utilitySelf(goodsSelf[0] + receivedGoods[0], goodsSelf[1] + receivedGoods[1]);
        return utility;
    }
    public boolean related(Individual other) {
        return Objects.equals(this.family, other.family);
    }
    public String bestDecision(Economy economy, int[] tempGoods) {
        String decision = "Self";
        double utility = utilitySelf(goodsSelf[0] + tempGoods[0], goodsSelf[1] + tempGoods[1]) - utilitySelf(goodsSelf[0], goodsSelf[1]);
        if (economy.get(family).leastUtils())
    }
    private double ln(int x) {
        return Math.log(x + 1);
    }
    private double ln(double x) {
        return Math.log(x + 1);
    }
    private double utilitySelf(int good1, int good2) {
        return preferences[0] * ln(good1) + preferences[1] * ln(good2);
    }

    @Override
    public int compareTo(Individual o) {
        return 0;
    }
}
