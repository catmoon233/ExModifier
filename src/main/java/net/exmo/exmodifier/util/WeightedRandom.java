package net.exmo.exmodifier.util;

import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.Optional;

public final class WeightedRandom {
    private WeightedRandom() {}

    public static <T> Optional<T> pick(RandomSource random, Map<T, Float> weights) {
        if (weights == null || weights.isEmpty()) return Optional.empty();
        float total = 0.0F;
        for (float weight : weights.values()) total += Math.max(0.0F, weight);
        if (total <= 0.0F) return Optional.empty();
        float roll = random.nextFloat() * total;
        float acc = 0.0F;
        T last = null;
        for (Map.Entry<T, Float> entry : weights.entrySet()) {
            last = entry.getKey();
            acc += Math.max(0.0F, entry.getValue());
            if (roll <= acc) return Optional.of(entry.getKey());
        }
        return Optional.ofNullable(last);
    }
}
