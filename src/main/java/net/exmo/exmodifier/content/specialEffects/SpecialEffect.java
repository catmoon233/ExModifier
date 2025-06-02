package net.exmo.exmodifier.content.specialEffects;

import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;
import java.util.function.Consumer;

public  class SpecialEffect {
    private final String id;
    private final Consumer<LivingEntity> livingEntityConsumer;

    public SpecialEffect(String id, Consumer<LivingEntity> livingEntityConsumer) {
        this.id = id;
        this.livingEntityConsumer = livingEntityConsumer;
    }

    public String id() {
        return id;
    }

    public Consumer<LivingEntity> livingEntityConsumer() {
        return livingEntityConsumer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SpecialEffect) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.livingEntityConsumer, that.livingEntityConsumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, livingEntityConsumer);
    }

    @Override
    public String toString() {
        return "SpecialEffect[" +
                "id=" + id + ", " +
                "livingEntityConsumer=" + livingEntityConsumer + ']';
    }


}