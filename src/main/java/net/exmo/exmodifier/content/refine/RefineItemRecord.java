package net.exmo.exmodifier.content.refine;

import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RefineItemRecord {
    public static  ExSerialize<RefineItemRecord> SERIALIZE = ExSerialize.create(
            ()->new RefineItemRecord(null,0,0,0)
    ).addResourceLocationField("item",RefineItemRecord::getItem,RefineItemRecord::setItem)
            .addIntField("maxBoostStar",RefineItemRecord::getMaxBoostStar,RefineItemRecord::setMaxBoostStar)
            .addIntField("chance",RefineItemRecord::getChance,RefineItemRecord::setChance)
            .addIntField("needMinStar",RefineItemRecord::getNeedMinStar,RefineItemRecord::setNeedMinStar);

    private ResourceLocation item;

    public int getMaxBoostStar() {
        return maxBoostStar;
    }

    public RefineItemRecord setMaxBoostStar(int maxBoostStar) {
        this.maxBoostStar = maxBoostStar;
        return this;
    }

    public ResourceLocation getItem() {
        return item;
    }

    public RefineItemRecord setItem(ResourceLocation item) {
        this.item = item;
        return this;
    }

    public int getChance() {
        return chance;
    }

    public RefineItemRecord setChance(int chance) {
        this.chance = chance;
        return this;
    }

    public int getNeedMinStar() {
        return needMinStar;
    }

    public RefineItemRecord setNeedMinStar(int needMinStar) {
        this.needMinStar = needMinStar;
        return this;
    }

    private  int maxBoostStar;
    private  int chance;
    private  int needMinStar;

    public RefineItemRecord(
            ResourceLocation item, int maxBoostStar, int chance, int needMinStar
    ) {
        this.item = item;
        this.maxBoostStar = maxBoostStar;
        this.chance = chance;
        this.needMinStar = needMinStar;
    }

    public ResourceLocation item() {
        return item;
    }

    public int maxBoostStar() {
        return maxBoostStar;
    }

    public int chance() {
        return chance;
    }

    public int needMinStar() {
        return needMinStar;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RefineItemRecord) obj;
        return Objects.equals(this.item, that.item) &&
                this.maxBoostStar == that.maxBoostStar &&
                this.chance == that.chance &&
                this.needMinStar == that.needMinStar;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, maxBoostStar, chance, needMinStar);
    }

    public List<Component> getDescriptionComponents() {
        List<Component> list = new ArrayList<>();
        list.add(Component.empty());
        list.add(Component.translatable("exmodifier.refine.description.title").withStyle(ChatFormatting.GOLD));
        list.add(Component.translatable("exmodifier.refine.description.max_boost_star", maxBoostStar).withStyle(ChatFormatting.AQUA));
        list.add(Component.translatable("exmodifier.refine.description.success_chance", chance).withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("exmodifier.refine.description.need_min_star", needMinStar).withStyle(ChatFormatting.YELLOW));
        return list;
    }

    @Override
    public String toString() {
        return "RefineItemRecord[" +
                "item=" + item + ", " +
                "maxBoostStar=" + maxBoostStar + ", " +
                "chance=" + chance + ", " +
                "needMinStar=" + needMinStar + ']';
    }

}
