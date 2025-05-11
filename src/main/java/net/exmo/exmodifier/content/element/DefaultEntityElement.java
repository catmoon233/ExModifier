package net.exmo.exmodifier.content.element;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DefaultEntityElement {
    // 在序列化器配置中添加对 JSON 对象列表的支持
    public static final ExSerialize<DefaultEntityElement> SERIALIZER = ExSerialize.create(() ->
                    new DefaultEntityElement(null, new ArrayList<>()))
            .withAutoId(DefaultEntityElement::getEntityTypeString,DefaultEntityElement::setEntityType)
            .addStringField("entityType",
                    DefaultEntityElement::getEntityTypeString,
                    DefaultEntityElement::setEntityType)
            .addJsonObjectList("entries",
                    DefaultEntityElement::getExElementInstantsString,
                    DefaultEntityElement::setExElementInstantsString);

    private List<JsonObject> getExElementInstantsString() {
        return  exElementInstants.stream().map(ExElementInstant.EX_SERIALIZE::toSingleJson).toList();
    }
    private void setExElementInstantsString(List<JsonObject> configs) {
        configs.forEach(config -> {
            // 解析每个配置对象中的 entries 数组
            var instants = ExElementInstant.EX_SERIALIZE.fromJsonSingle(
                    config
            );
            exElementInstants.add(instants);
        });
    }
    private void setEntityType(String s) {
        Optional<EntityType<?>> entityType = EntityType.byString(s);
        entityType.ifPresent(type -> this.entityType = type);
    }

    private EntityType<?> entityType;

    public List<ExElementInstant> getExElementInstants() {
        return exElementInstants;
    }

    public DefaultEntityElement setExElementInstants(List<ExElementInstant> exElementInstants) {
        this.exElementInstants = exElementInstants;
        return this;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public String getEntityTypeString() {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        if (key == null) return "";
        return key.toString();
    }

    public DefaultEntityElement setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
        return this;
    }

    private List<ExElementInstant> exElementInstants;

    public DefaultEntityElement(EntityType<?> entityType,
                                List<ExElementInstant> exElementInstants) {
        this.entityType = entityType;
        this.exElementInstants = exElementInstants;
    }

    public EntityType<?> entityType() {
        return entityType;
    }

    public List<ExElementInstant> exElementInstants() {
        return exElementInstants;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DefaultEntityElement) obj;
        return Objects.equals(this.entityType, that.entityType) &&
                Objects.equals(this.exElementInstants, that.exElementInstants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, exElementInstants);
    }

    @Override
    public String toString() {
        return "DefaultEntityElement[" +
                "entityType=" + entityType + ", " +
                "exElementInstants=" + exElementInstants + ']';
    }


}
