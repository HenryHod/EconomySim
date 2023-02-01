import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;

public class Economy {
    private int[] totalGoods;

    private HashMap<Integer, Family> families;
    public Economy(int size, Random rand) {
        families = new HashMap<>();
        totalGoods = new int[]{0, 0};
        for (int i = 0; i < size; i++) {
            int apples = rand.nextInt(1, 20);
            int oranges = rand.nextInt(1, 20);
            families.put(i, new Family( new Individual(rand, i, apples, oranges)));
            totalGoods[0] += apples;
            totalGoods[1] += oranges;
        }
    }
    public Individual findLeastUtils(Individual self) {

        PriorityQueue<Individual> leastUtilsIndividual = new PriorityQueue<>();
        for (int i = Math.max(0, self.family - 5);i < Math.min(families.size(), self.family + 6); i++) {
            if (i != self.family) {
                leastUtilsIndividual.add(families.get(i).leastUtils());
            }
        }
        return leastUtilsIndividual.poll();
    }
    public void period() {
        totalGoods = new int[]{0, 0};
        for (Integer family: families.keySet()) {
            for (Individual familyMember : families.get(family)) {
                if (familyMember.goods[0] + familyMember.goods[1] == 0) {
                    families.get(family).remove(familyMember);
                }
                familyMember.individualTurn(this);
                totalGoods[0] += familyMember.goods[0];
                totalGoods[1] += familyMember.goods[1];
            }
        }
    }
    public Family get(Integer i) {
        return families.get(i);
    }
    public void print() {
        System.out.println(families.size());
        System.out.println(totalGoods[0] + " " + totalGoods[1]);
        System.out.println(families.keySet().stream().mapToDouble(key -> families.get(key).totalUtility()).sum());
    }
}
