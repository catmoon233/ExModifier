package net.exmo.exmodifier.content.element;

import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class ExElementInstant {
    public static final ExSerialize<ExElementInstant> EX_SERIALIZE = ExSerialize.create(()-> new ExElementInstant(null,0))
            .addStringField("id", exElementInstant -> exElementInstant.getElement().getResId().toString(),(exElementInstant, s) -> exElementInstant.setElement(ExElementHandle.getExElement(s)))
            .addIntField("level", ExElementInstant::getLevel,ExElementInstant::setLevel);
    private ExElement element;
    private int level;

    public ExElementInstant multiply(float i) {
        this.level *= i;
        return this;
    }

    public Component getDesc() {
        return Component.translatable("modifier.element.%s".formatted(element.getResId())).append(Component.literal(" ").append(Component.translatable("exmodifier.element.level",level).withStyle(ChatFormatting.GRAY)));
    }
    public CompoundTag getData() {
        return data;
    }

    public ExElementInstant setData(CompoundTag data) {
        this.data = data;
        return this;
    }

    private CompoundTag data;

    public ExElement getElement() {
        return element;
    }
    public static ExElementInstant of (ExElement element, int level) {
        return new ExElementInstant(element, level);
    }    public static ExElementInstant of (ExElement element) {
        return new ExElementInstant(element,1);
    }

    public ExElementInstant setElement(ExElement element) {
        this.element = element;
        return this;
    }

    public int getLevel() {
        return level;
    }

    public ExElementInstant setLevel(int level) {
        this.level = level;
        return this;
    }
    public ExElementInstant(ExElement element, int level) {
        this.element = element;
        this.level = level;
    }
}
