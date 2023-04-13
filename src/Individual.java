import java.util.*;

public class Individual implements Comparable<Individual>, Iterable<Integer>{
    double currentUtility;
    double currentSelfUtility;
    int birth;
    int age;
    final int id;
    Integer family;
    Integer clan;
    int[] goods;
    int[] goodsSelf;
    int[] goodsFuture;
    int skills;
    double altruism;
    double charity;
    double impatience;
    double[] preferences;
    Individual parent;
    //HashSet<Individual> siblings;
    HashSet<Integer> children;

    public Individual(Random rand, Integer familyNumber, int good1, int good2, int i) {
        family = familyNumber;
        clan = 0;
        age = 0;
        birth = 0;
        id = i;
        skills = rand.nextInt(2,11);
        altruism = rand.nextDouble(0.5, 1.0);
        charity = 0.5;//rand.nextDouble(1.0);
        impatience = 0.99;//rand.nextDouble(0.5, 1.0);
        double applePreference = rand.nextDouble(1.0);
        preferences = new double[]{applePreference, rand.nextDouble(1.0 - applePreference)};
        goods = new int[]{good1, good2};
        goodsSelf = new int[]{0, 0};
        goodsFuture = new int[]{0, 0};
        children = new HashSet<>();
        //siblings = new HashSet<>();
    }
    public Individual(Random rand, Integer familyNumber, Individual p, int good1, int good2, int i, int period) {
        family = familyNumber;
        parent = p;
        clan = p.clan;
        age = 0;
        birth = period;
        id = i;
        skills = rand.nextInt(Math.max(parent.skills - 2, 2), Math.min(parent.skills + 2, 11));
        altruism = p.altruism; //Math.max(Math.min(rand.nextGaussian(0.8, 0.05), 1), 0);
        charity = 0.5; //Math.max(Math.min(rand.nextGaussian(parent.charity, 0.05), 1), 0);
        impatience = p.impatience; //Math.max(Math.min(rand.nextGaussian(0.75, 0.05), 1), 0);
        double applePreference = 0.275;//rand.nextDouble(1.0);
        preferences = new double[]{applePreference, 0.275};//rand.nextDouble(1.0 - applePreference)};
        goods = new int[]{good1, good2};
        goodsSelf = new int[]{0, 0};
        goodsFuture = new int[]{0, 0};
        children = new HashSet<>();
        //siblings = new HashSet<>(p.children);
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
    private void bestDecision(Economy economy, int good1, int good2, Individual familyMember, Individual charityCase) {
        double utility = utilityDiff(good1, good2);
        //- utilityFutDiff(good1, good2);
        String decision = "Self";
        //System.out.println(charityCase);
        //System.out.println("self utility: " + utility);
        //System.out.println("future consumption: " + goodsFuture[0] + " " + goodsFuture[1]);
        //System.out.println("charity: " + charity * charityCase.utilityDiff(good1, good2));
        //System.out.println("Family: " + altruism * familyMember.utilityDiff(good1, good2));
        System.out.println(impatience * utilityFutDiff(good1, good2) + " " + utilityDiff(good1, good2));
        if (impatience * utilityFutDiff(good1, good2) > utilityDiff(good1, good2)) {
            //System.out.println("Production");
            decision = "Production";
            utility += impatience * utilityFutDiff(good1, good2);
        }
        if (familyMember != null && utility < altruism) {
            if (altruism * familyMember.utilityDiff(good1, good2) > utility) {
                //System.out.println("Family");
                decision = "Family";
                utility += altruism * familyMember.utilityDiff(good1, good2);
            }
        }
        if (charityCase != null && utility < charity) {
            if (charity * charityCase.utilityDiff(good1, good2) > utility) {
                //System.out.println("Charity");
                decision = "Charity";
            }
        }
        executeDecision(economy, decision, good1, good2, familyMember, charityCase, utility);
    }
    private void executeDecision(Economy economy, String decision, int good1, int good2, Individual familyMember, Individual charityCase,Double utility) {
        if (Objects.equals(decision, "Self")) {
            goodsSelf[0] += good1;
            goodsSelf[1] += good2;
            currentSelfUtility += utility;
            currentUtility += utility;
        } else if (Objects.equals(decision, "Production")) {
            int r = economy.random.nextInt(Math.max(0, skills - 2), Math.min(skills + 2, 10));
            goodsFuture[0] += good1 * skills;
            goodsFuture[1] += good2 * skills;
        } else if (Objects.equals(decision, "Family")) {
            currentUtility += altruism * utility;
            familyMember.addGoods(good1, good2);
            familyMember.currentUtility += utility;
        } else if (Objects.equals(decision, "Charity")) {
            currentUtility = charity * utility;
            charityCase.addGoods(good1, good2);
            charityCase.currentUtility += utility;
        }
    }
    public void individualTurn(Economy economy) {
        goodsSelf = new int[]{0, 0};
        goodsFuture = new int[]{0, 0};
        Individual familyMember = null;
        Individual charityCase = null;
        Individual tradingPartner = null;
        currentSelfUtility = 0;
        currentUtility = 0;
        if (economy.get(family).get(clan).living() > 1) {
            familyMember = economy.get(family).getOne(economy.random).getOne(economy.random);
        }
        if (economy.size() > 1) {
            charityCase = economy.getOne(this);
        }
        //System.out.println(goodsSelf[0] + " " + goodsSelf[1]);
        //System.out.println((1 + (skills / impatience )) * goodsSelf[0] + " > or < " + ((skills / impatience) * (goods[0]) + goodsFuture[0]));
        //System.out.println((skills + 1) * goodsSelf[1] + " > or < " + (skills * (goods[1]) + goodsFuture[1]));
        //System.out.println(goods[0] + " " + goods[1]);
        //System.out.println("child utility: " + altruism * basicUtility(10, 10) + "self utility: " + utilityDiff(10, 10));
        if ((goods[0] >= economy.childCost / 2 && goods[1] >= economy.childCost / 2) && (altruism * basicUtility(economy.childCost / 2, economy.childCost / 2) > utilityDiff(economy.childCost / 2, economy.childCost / 2))) {
            //System.out.println("child utility: " + utilityChildDiff() + "self for same goods " + utilityDiff(5, 5));
            //System.out.println("family size: " + economy.get(family).size());
            //System.out.println("Before: " + economy.get(family).size());
            Individual child = new Individual(economy.random, family,this, economy.childCost / 2, economy.childCost / 2, economy.get(family).size() + 1, economy.currentPeriod());
            addChild(child);
            economy.add(family, child);
            //System.out.println("After: " + economy.get(family).size());
            goods[0] -= economy.childCost / 2;
            goods[1] -= economy.childCost / 2;
            currentUtility += altruism * child.potentialUtility();

        } else {
            if (goods[0] > 0 & goods[1] > 0) {
                bestDecision(economy, 1, 1, familyMember, charityCase);
                goods[0] -= 1;
                goods[1] -= 1;
            } else if (goods[0] > 0 && goods[1] == 0) {
                bestDecision(economy, 1, 0, familyMember, charityCase);
                goods[0] -= 1;
            } else if (goods[0] == 0) {
                bestDecision(economy, 0, 1, familyMember, charityCase);
                goods[1] -= 1;
            }
        }
        //System.out.println(apples + " " + oranges + " " + utilityChildDiff() + " " + utilityDiff(5, 5));
    }
    private double utilitySelf(int good1, int good2) {
        return Math.pow(goodsSelf[0] + good1 + 1, preferences[0]) + Math.pow(goodsSelf[1] + good2 + 1, preferences[1]) - 1;
    }
    private double utilitySelf() {
            return currentSelfUtility;
    }
    private double utilityDiff(int good1, int good2) {
        //System.out.println(utilitySelf(good1, good2) + " " + utilitySelf());
        return good1 * preferences[0] * Math.pow(goodsSelf[0] + 1, preferences[0] - 1)  * Math.pow(goodsSelf[1] + 1, preferences[1]) +
                good2 * preferences[1] * Math.pow(goodsSelf[0] + 1, preferences[0]) * Math.pow(goodsSelf[1] + 1, preferences[1] - 1);
    }
    private double utilitySkillsDiff(int good1, int good2) {
        return good1 * preferences[0] * Math.pow(goodsSelf[0] + skills , preferences[0] - 1)  * Math.pow(goodsSelf[1] + skills, preferences[1]) +
                good2 * preferences[1] * Math.pow(goodsSelf[0] + skills, preferences[0]) * Math.pow(goodsSelf[1] + skills, preferences[1] - 1);
    }
    private double utilityFutDiff(int good1, int good2) {
        return good1 * preferences[0] * Math.pow(goodsFuture[0] + 1 , preferences[0] - 1)  * Math.pow(goodsFuture[1] + 1, preferences[1]) +
                good2 * preferences[1] * Math.pow(goodsFuture[0] + 1, preferences[0]) * Math.pow(goodsFuture[1] + 1, preferences[1] - 1);
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
        children.add(child.id);
        //System.out.println(children.size());
    }
    /*
    private boolean consumptionCheck() {
        double denom = 0.0;
        for (int i = 0; i < 5 - age; i++) {
            denom += Math.pow(impatience / skills, i);
        }
        if (children.size() > 0) {
            for (Integer child: children) {
                for (int i = 0; i < 5 - age; i++) {
                    denom += altruism * Math.pow(child.impatience / child.skills, i);
                }
            }
        }
        return goodsSelf[0] > goods[0] / denom | goodsSelf[1] > goods[0] / denom;
    }

     */
    public void addPeriod() {
        age++;
    }
    public int goodTotals() {
        return goods[0] + goods[1];
    }
    public double potentialUtility() {
        return Math.pow(goods[0] + 1, preferences[0]) * Math.pow(goods[1] + 1, preferences[1]) - 1;
    }
    public double basicUtility(int good1, int good2) {
        return Math.pow(good1 + 1, preferences[0]) * Math.pow(good2 + 1, preferences[1]);
    }
    public String dataEntry() {
        return "" + family
                + ", " + id
                + ", " + age
                + ", " + children.size()
                + ", " + clan
                + ", " + altruism
                + ", " + impatience
                + ", " + charity
                + ", " + skills
                + ", " + goods[0]
                + ", " + goods[1]
                + ", " + goodsSelf[0]
                + ", " + goodsSelf[1]
                + ", " + preferences[0]
                + ", " + preferences[1]
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
        child.addGoods(goods[0] / children.size(), goods[1] / children.size());
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
    public boolean hasGoods() { return goods[0] + goods[1] > 0;}
    public void setFutureGoods() {
        goods = goodsFuture;
    }
    @Override
    public Iterator<Integer> iterator() {
        return children.iterator();
    }
}
