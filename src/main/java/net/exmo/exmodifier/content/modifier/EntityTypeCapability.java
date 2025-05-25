package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntityTypeCapability implements BaseCapability {
    private static final ResourceLocation KEY = new ResourceLocation("exmodifier", "entity_type");
    private List<String> entityTypes = new ArrayList<>();
    private final ExSerialize<EntityTypeCapability> serializer;

    public EntityTypeCapability() {
        this.serializer = ExSerialize.create(EntityTypeCapability::new)
                .addStringListField("entityTypes", EntityTypeCapability::getEntityTypes, EntityTypeCapability::setEntityTypes);
    }

    public List<String> getEntityTypes() {
        return Collections.unmodifiableList(entityTypes);
    }

    public void setEntityTypes(List<String> entityTypes) {
        this.entityTypes = new ArrayList<>(entityTypes);
    }

    @Override
    public ResourceLocation getKey() {
        return KEY;
    }

    @Override
    public ExSerialize<? extends BaseCapability> getSerializer() {
        return serializer;
    }

    static {
        CapabilityManager.register(new EntityTypeCapability());
    }
}