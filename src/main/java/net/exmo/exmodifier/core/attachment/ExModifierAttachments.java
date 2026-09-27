package net.exmo.exmodifier.core.attachment;

import net.exmo.exmodifier.Exmodifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ExModifierAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Exmodifier.MODID);

    public static final Supplier<AttachmentType<EntityElements>> ENTITY_ELEMENTS = ATTACHMENTS.register("entity_elements",
            () -> AttachmentType.builder(() -> EntityElements.EMPTY)
                    .serialize(EntityElements.CODEC)
                    .sync(EntityElements.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<CustomEffects>> CUSTOM_EFFECTS = ATTACHMENTS.register("custom_effects",
            () -> AttachmentType.builder(() -> CustomEffects.EMPTY)
                    .serialize(CustomEffects.CODEC)
                    .sync(CustomEffects.STREAM_CODEC)
                    .build());

    private ExModifierAttachments() {}

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}
