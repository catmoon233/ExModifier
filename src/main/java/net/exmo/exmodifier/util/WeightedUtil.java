package net.exmo.exmodifier.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WeightedUtil {

    public Map<String, Float> weights = new HashMap<>();
    private float totalWeight;
    private float[] cumulativeWeights;

    public WeightedUtil(Map<String, Float> weights) {
        this.weights = new HashMap<>(weights);
        this.totalWeight = calculateTotalWeight();
        this.cumulativeWeights = calculateCumulativeWeights();
    }

    private float calculateTotalWeight() {
        float total = 0f;
        for (Float weight : weights.values()) {
            total += weight;
        }
        return total;
    }

    private float[] calculateCumulativeWeights() {
        float[] cumulativeWeights = new float[weights.size()];
        float sum = 0f;
        int index = 0;
        for (Float weight : weights.values()) {
            sum += weight;
            cumulativeWeights[index++] = sum;
        }
        return cumulativeWeights;
    }

    /**
     * 获取指定权重出现的概率。
     * @param key 键
     * @return 概率值
     */
    public float getProbability(String key) {
        Float weight = weights.get(key);
        return weight != null ? weight / totalWeight : 0f;
    }

    /**
     * 根据权重随机选择一个键。
     * @return 被选中的键
     */
    public String selectRandomKeyBasedOnWeights() {
        Random random = new Random();
        float value = random.nextFloat() * totalWeight;
        int index = findIndexForValue(value);

        // Ensure that there are elements in the map before converting to array
        if (weights.isEmpty()) {
            return null; // Or throw an exception, depending on your use case
        }

        return weights.keySet().toArray(new String[0])[index];
    }

    private int findIndexForValue(float value) {
        int index = -1;
        for (int i = 0; i < cumulativeWeights.length; i++) {
            if (value <= cumulativeWeights[i]) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 提升所有权重值。
     * @param rarity 稀有度
     */
    public void increaseWeightsByRarity(int rarity) {
        Map<String, Float> newWeights = new HashMap<>();
        for (Map.Entry<String, Float> entry : weights.entrySet()) {
            float newWeight = entry.getValue() + (rarity / 2) / entry.getValue();

            // Ensure the new weight is not negative
            if (newWeight <= 0f) {
                newWeight = 1f; // Set a minimum value
            }

            newWeights.put(entry.getKey(), newWeight);
        }
        weights = newWeights;
        totalWeight = calculateTotalWeight();
        cumulativeWeights = calculateCumulativeWeights();
    }
}