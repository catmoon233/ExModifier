package net.exmo.exmodifier.core.component;

import net.exmo.exmodifier.Exmodifier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ExModifierComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Exmodifier.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AppliedModifiers>> MODIFIERS =
            COMPONENTS.registerComponentType("modifiers", builder -> builder
                    .persistent(AppliedModifiers.CODEC)
                    .networkSynchronized(AppliedModifiers.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemElements>> ELEMENTS =
            COMPONENTS.registerComponentType("elements", builder -> builder
                    .persistent(ItemElements.CODEC)
                    .networkSynchronized(ItemElements.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemQualityRef>> QUALITY =
            COMPONENTS.registerComponentType("quality", builder -> builder
                    .persistent(ItemQualityRef.CODEC)
                    .networkSynchronized(ItemQualityRef.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RefineState>> REFINE =
            COMPONENTS.registerComponentType("refine", builder -> builder
                    .persistent(RefineState.CODEC)
                    .networkSynchronized(RefineState.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UnlockedSlots>> SLOTS =
            COMPONENTS.registerComponentType("slots", builder -> builder
                    .persistent(UnlockedSlots.CODEC)
                    .networkSynchronized(UnlockedSlots.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EntryBook>> ENTRY_BOOK =
            COMPONENTS.registerComponentType("entry_book", builder -> builder
                    .persistent(EntryBook.CODEC)
                    .networkSynchronized(EntryBook.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemLevelState>> ITEM_LEVEL =
            COMPONENTS.registerComponentType("item_level", builder -> builder
                    .persistent(ItemLevelState.CODEC)
                    .networkSynchronized(ItemLevelState.STREAM_CODEC));

    private ExModifierComponents() {}

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }

    public static DataComponentType<AppliedModifiers> modifiers() {
        return MODIFIERS.get();
    }
}
