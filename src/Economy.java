import org.json.JSONObject;

import java.util.*;

public class Economy {
    private int[] totalGoods;
    public JSONObject jsonObject;
    private int periodCount;
    private JSONObject currentPeriod;
    public Random random;
    private HashMap<Integer, Family> families;
    public Economy(int size, Random rand, JSONObject jo) {
        random = rand;
        families = new HashMap<>();
        totalGoods = new int[]{0, 0};
        periodCount = 0;
        jsonObject = jo;
        jsonObject.getJSONObject("periods").put(String.valueOf(periodCount), new JSONObject("{\"families\": {}}"));
        currentPeriod = jsonObject.getJSONObject("periods").getJSONObject(String.valueOf(periodCount));
        for (int i = 0; i < size; i++) {
            int apples = 100; //rand.nextInt(1, 100);
            int oranges = 100; //rand.nextInt(1, 100);
            families.put(i, new Family( new Individual(rand, i, apples, oranges, 0)));
            totalGoods[0] += apples;
            totalGoods[1] += oranges;
            currentPeriod.getJSONObject("families").put(String.valueOf(i), new JSONObject("{\"size\": 1 }"));
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
    public void period() {
        totalGoods = new int[]{0, 0};
        periodCount++;
        jsonObject.getJSONObject("periods").put(String.valueOf(periodCount), new JSONObject("{\"families\": {}}"));
        currentPeriod = jsonObject.getJSONObject("periods").getJSONObject(String.valueOf(periodCount));
        for (Integer family: new HashSet<>(families.keySet())) {
            for (Individual familyMember : families.get(family)) {
                if (familyMember.goods[0] + familyMember.goods[1] == 0 | familyMember.age >= 5) {
                    families.get(family).remove(familyMember);
                    if (families.get(family).size() <= 0) {
                        families.remove(family);
                    } else {
                        currentPeriod.getJSONObject("families").put(String.valueOf(family), new JSONObject().put("size", families.get(family).size()));
                    }
                    //System.out.println("family left:" + families.get(family).size());
                } else {
                    if (!currentPeriod.getJSONObject("families").keySet().contains(String.valueOf(family))) {
                        currentPeriod.getJSONObject("families").put(String.valueOf(family), new JSONObject().put("size", families.get(family).size()));
                    }
                    if (!currentPeriod.getJSONObject("families").getJSONObject(String.valueOf(family)).keySet().contains("individuals")) {
                        currentPeriod.getJSONObject("families").getJSONObject(String.valueOf(family)).put("individuals", new JSONObject());
                    }
                    if (familyMember.age >= 1) {
                        familyMember.individualTurn(this);
                    }
                    familyMember.addInfo(currentPeriod.getJSONObject("families").getJSONObject(String.valueOf(family)).getJSONObject("individuals"));
                    currentPeriod.getJSONObject("families").getJSONObject(String.valueOf(family)).put("utility", families.get(family).totalUtility());
                    int[] familyGoods = families.get(family).totalGoods();
                    currentPeriod.getJSONObject("families").getJSONObject(String.valueOf(family)).put("good1", familyGoods[0]);
                    currentPeriod.getJSONObject("families").getJSONObject(String.valueOf(family)).put("good2", familyGoods[1]);
                    familyMember.addPeriod();

                }
                totalGoods[0] += familyMember.goods[0];
                totalGoods[1] += familyMember.goods[1];
            }
            if (families.containsKey(family)) {
                currentPeriod.getJSONObject("families").getJSONObject(String.valueOf(family)).put("size", families.get(family).size());
            }

        }
    }
    public void add(Integer family,Individual i) {
        families.get(family).add(i);
        System.out.println(families.get(family).size());
        jsonObject.getJSONObject("periods").getJSONObject(String.valueOf(periodCount))
                .getJSONObject("families").getJSONObject(String.valueOf(family))
                .put("size", families.get(family).size());
    }
    public Family get(Integer i) {
        return families.get(i);
    }
    public int size() {
        return families.size();
    }
    public void print() {
        System.out.println(families.keySet().stream().mapToInt(key -> families.get(key).size()).sum());
        System.out.println(totalGoods[0] + " " + totalGoods[1]);
        System.out.println(families.keySet().stream().mapToDouble(key -> families.get(key).totalUtility()).sum());
    }
}
