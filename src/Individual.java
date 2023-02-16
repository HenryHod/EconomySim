import org.json.JSONObject;

import java.util.*;

public class Individual implements Comparable<Individual>{
    double currentUtility;
    int age;
    private final int id;
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

    public Individual(Random rand, Integer familyNumber, int good1, int good2, int i) {
        family = familyNumber;
        age = 0;
        id = i;
        skills = rand.nextInt(2,5);
        altruism = Math.min(rand.nextGaussian(0.85, 0.05), 0.99);
        charity = rand.nextDouble(1.0);
        impatience = Math.min(rand.nextGaussian(0.75, 0.05), 0.99);
        double applePreference = rand.nextDouble(1.0);
        preferences = new double[]{applePreference, 1 - applePreference};
        goods = new int[]{good1, good2};
        goodsSelf = new int[]{0, 0};
        goodsFuture = new int[]{0, 0};
        goodsFamily = new HashMap<>();
        goodsCharity = new HashMap<>();
        children = new HashSet<>();
    }
    public Individual(Random rand, Integer familyNumber, Individual p, int good1, int good2, int i) {
        family = familyNumber;
        parent = p;
        age = 0;
        id = i;
        skills = rand.nextInt(Math.max(parent.skills - 2, 1), Math.min(parent.skills + 2, 5));
        altruism = Math.min(rand.nextGaussian(parent.altruism, 0.01), 1);
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
        goods[0] += apples;
        goods[1] += oranges;
        currentUtility += utilityDiff(apples, oranges);


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
        //System.out.println("future consumption: " + goodsFuture[0] + " " + goodsFuture[1]);
        //System.out.println("charity: " + charity * charityCase.utilityDiff(good1, good2));
        //System.out.println("Family: " + altruism * familyMember.utilityDiff(good1, good2));
        if (impatience * utilityFutDiff(good1, good2) > utility) {
            //System.out.println("Production");
            decision = "Production";
            utility += impatience * utilityFutDiff(good1, good2);
        }
        if (altruism * familyMember.utilityDiff(good1, good2) > utility) {
            //System.out.println("Family");
            decision = "Family";
            utility += altruism * familyMember.utilityDiff(good1, good2);
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
            int r = economy.random.nextInt(Math.max(0, skills - 2), Math.min(skills + 2, 10));
            goodsFuture[0] += good1 * r;
            goodsFuture[1] += good2 * r;
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
        goodsSelf = new int[]{0, 0};
        goodsFuture = new int[]{0, 0};
        int apples = goods[0];
        int oranges = goods[1];
        //System.out.println(goodsSelf[0] + " " + goodsSelf[1]);
        while (apples > 0 | oranges > 0) {
            //System.out.println((1 + (skills / impatience )) * goodsSelf[0] + " > or < " + ((skills / impatience) * (goods[0]) + goodsFuture[0]));
            //System.out.println((skills + 1) * goodsSelf[1] + " > or < " + (skills * (goods[1]) + goodsFuture[1]));
            //System.out.println(goods[0] + " " + goods[1]);
            System.out.println("child utility: " + altruism * basicUtility(10, 10));
            if ((oranges >= 10 & apples >= 10) & (altruism * basicUtility(10, 10) > utilitySelf(10, 10) & age >= 2)) {
                //System.out.println("child utility: " + utilityChildDiff() + "self for same goods " + utilityDiff(5, 5));
                //System.out.println("family size: " + economy.get(family).size());
                //System.out.println("Before: " + economy.get(family).size());
                Individual child = new Individual(economy.random, family,this, 10, 10, economy.get(family).size() + 1);
                addChild(child);
                economy.add(family, child);
                //System.out.println("After: " + economy.get(family).size());
                apples -= 10;
                oranges -= 10;
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
        return 10 * Math.pow(goodsSelf[0] + good1 + 1, preferences[0]) * Math.pow(goodsSelf[1] + good2 + 1, preferences[1]) - 1;
    }
    private double utilitySelf() {
            return 10 * Math.pow(goodsSelf[0] + 1, preferences[0]) * Math.pow(goodsSelf[1] + 1, preferences[1]) - 1;
    }
    private double utilityDiff(int good1, int good2) {
        //System.out.println(utilitySelf(good1, good2) + " " + utilitySelf());
        return (utilitySelf(good1, good2) - utilitySelf());
    }
    private double utilityFutDiff(int good1, int good2) {
        double previous = 10 * (Math.pow(goodsFuture[0] + 1, preferences[0]) * Math.pow(goodsFuture[1] + 1, preferences[1]) - 1);
        double current = 10 * (Math.pow(goodsFuture[0] + good1 * skills + 2, preferences[0]) * Math.pow(goodsFuture[1] + good2 * skills + 1, preferences[1]) - 1);
        return (current - previous)/skills;
    }
    private double ln(int x) {
        return Math.log(x + 1);
    }
    private double utilityChildDiff() {
        //System.out.println("z value: " + ((double) children.size() + 1.0 - childrenUtility.getNumericalMean())/Math.pow(childrenUtility.getNumericalVariance(), 0.5));
        //System.out.println("Random Chi Values: " + childrenUtility.density(5.0) + " " + childrenUtility.density(3.00));
        //System.out.println("child utility: " + (altruism * 5 + 5 * childrenUtility.density((double) children.size() + 1.0)));
        return altruism  * (ln(children.size() + 1) - ln(children.size()) + 10);
    }
    public double getCurrentUtility() {
        return currentUtility;
    }
    private void addChild(Individual child) {
        children.add(child);
        //System.out.println(children.size());
    }
    private boolean consumptionCheck() {
        double denom = 0.0;
        for (int i = 0; i < 5 - age; i++) {
            denom += Math.pow(impatience / skills, i);
        }
        if (children.size() > 0) {
            for (Individual child: children) {
                for (int i = 0; i < 5 - age; i++) {
                    denom += altruism * Math.pow(child.impatience / child.skills, i);
                }
            }
        }
        return goodsSelf[0] > goods[0] / denom | goodsSelf[1] > goods[0] / denom;
    }
    public void removeSelf() {
        if ((goods[0] > 0 | goods[1] > 0) & children.size() > 0) {
            for (Individual child: children) {
                child.addGoods(goods[0] / children.size(), goods[1] / children.size());
            }
        }
    }
    public void addPeriod() {
        age++;
    }
    public void addInfo(JSONObject familyJson) {
        familyJson.put(String.valueOf(id), new JSONObject());
        familyJson.getJSONObject(String.valueOf(id)).put("id", id);
        familyJson.getJSONObject(String.valueOf(id)).put("children", children.size());
        familyJson.getJSONObject(String.valueOf(id)).put("altruism", altruism);
        familyJson.getJSONObject(String.valueOf(id)).put("charity", charity);
        familyJson.getJSONObject(String.valueOf(id)).put("impatience", impatience);
        familyJson.getJSONObject(String.valueOf(id)).put("skills", skills);
        familyJson.getJSONObject(String.valueOf(id)).put("good1 pref", preferences[0]);
        familyJson.getJSONObject(String.valueOf(id)).put("good2 pref", preferences[1]);
        familyJson.getJSONObject(String.valueOf(id)).put("good1", goods[0]);
        familyJson.getJSONObject(String.valueOf(id)).put("good2", goods[1]);
        familyJson.getJSONObject(String.valueOf(id)).put("future good1", goodsFuture[0]);
        familyJson.getJSONObject(String.valueOf(id)).put("future good2", goodsFuture[1]);
    }
    public boolean isChild(Individual i) {
        return children.contains(i);
    }
    public boolean isGrandchild(Individual i) {
        return children.stream().anyMatch(child -> child.isChild(i));
    }
    public int goodTotals() {
        return goods[0] + goods[1];
    }
    public double potentialUtility() {
        return Math.pow(goods[0] + 1, preferences[0]) * Math.pow(goods[1] + 1, preferences[1]) - 1;
    }
    public double basicUtility(int good1, int good2) {
        return 10 * Math.pow(good1 + 1, preferences[0]) * Math.pow(good2 + 1, preferences[1]);
    }
    @Override
    public int compareTo(Individual o) {
        return (int) (currentUtility - o.currentUtility);
    }
}
