package net.exmo.exmodifier.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;
import java.util.Optional;

public record AttributeSpec(
        ResourceLocation attribute,
        double amount,
        double amountPerLevel,
        AttributeModifier.Operation operation,
        Optional<String> expression
) {
    public static final Codec<AttributeSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("attribute").forGetter(AttributeSpec::attribute),
            Codec.DOUBLE.optionalFieldOf("amount", 0.0D).forGetter(AttributeSpec::amount),
            Codec.DOUBLE.optionalFieldOf("amount_per_level", 0.0D).forGetter(AttributeSpec::amountPerLevel),
            StringRepresentable.fromEnum(AttributeModifier.Operation::values)
                    .optionalFieldOf("operation", AttributeModifier.Operation.ADD_VALUE)
                    .forGetter(AttributeSpec::operation),
            Codec.STRING.optionalFieldOf("expression").forGetter(AttributeSpec::expression)
    ).apply(instance, AttributeSpec::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeSpec> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, AttributeSpec::attribute,
            ByteBufCodecs.DOUBLE, AttributeSpec::amount,
            ByteBufCodecs.DOUBLE, AttributeSpec::amountPerLevel,
            ByteBufCodecs.VAR_INT.map(id -> AttributeModifier.Operation.values()[id], AttributeModifier.Operation::ordinal), AttributeSpec::operation,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional), AttributeSpec::expression,
            AttributeSpec::new
    );

    public double amountAt(int level) {
        int safe = Math.max(1, level);
        if (expression.isPresent() && !expression.get().isBlank()) {
            return LevelExpression.evaluate(expression.get(), safe);
        }
        return amount + amountPerLevel * (safe - 1);
    }
}
