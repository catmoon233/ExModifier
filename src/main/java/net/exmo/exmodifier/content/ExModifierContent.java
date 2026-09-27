package net.exmo.exmodifier.content;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.block.EmbeddedTableBlock;
import net.exmo.exmodifier.content.block.RefreshTableBlock;
import net.exmo.exmodifier.content.block.entity.EmbeddedTableEntity;
import net.exmo.exmodifier.content.block.entity.RefreshTableEntity;
import net.exmo.exmodifier.content.item.EntryItem;
import net.exmo.exmodifier.content.menu.EmbeddedMenu;
import net.exmo.exmodifier.content.menu.RefreshMenu;
import net.exmo.exmodifier.init.ExAttributes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;

public final class ExModifierContent {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Exmodifier.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Exmodifier.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Exmodifier.MODID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Exmodifier.MODID);
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Exmodifier.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Exmodifier.MODID);

    public static final DeferredBlock<Block> REFRESH_TABLE = BLOCKS.register("refresh_table",
            () -> new RefreshTableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> EMBEDDED_TABLE = BLOCKS.register("embedded_table",
            () -> new EmbeddedTableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(2.5F).sound(SoundType.WOOD)));

    public static final DeferredItem<Item> ENTRY_ITEM = ITEMS.register("entry_item", () -> new EntryItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> ENTRY_ITEM_EMPTY = ITEMS.register("entry_item_empty", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<BlockItem> REFRESH_TABLE_ITEM = ITEMS.register("refresh_table",
            () -> new BlockItem(REFRESH_TABLE.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> EMBEDDED_TABLE_ITEM = ITEMS.register("embedded_table",
            () -> new BlockItem(EMBEDDED_TABLE.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RefreshTableEntity>> REFRESH_TABLE_ENTITY =
            BLOCK_ENTITIES.register("refresh_table",
                    () -> BlockEntityType.Builder.of(RefreshTableEntity::new, REFRESH_TABLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmbeddedTableEntity>> EMBEDDED_TABLE_ENTITY =
            BLOCK_ENTITIES.register("embedded_table",
                    () -> BlockEntityType.Builder.of(EmbeddedTableEntity::new, EMBEDDED_TABLE.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<RefreshMenu>> REFRESH_MENU =
            MENUS.register("refresh_menu", () -> IMenuTypeExtension.create((id, inventory, buf) -> new RefreshMenu(id, inventory)));
    public static final DeferredHolder<MenuType<?>, MenuType<EmbeddedMenu>> EMBEDDED_MENU =
            MENUS.register("embedded_menu", () -> IMenuTypeExtension.create((id, inventory, buf) -> new EmbeddedMenu(id, inventory)));

    public static final DeferredHolder<MobEffect, MobEffect> DODGE = EFFECTS.register("dodge_effect",
            () -> new ScaledAttributeEffect(MobEffectCategory.BENEFICIAL, 0x7EC8E3, ExAttributes.DODGE, Exmodifier.id("effect/dodge"), 0.02D, 3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    public static final DeferredHolder<MobEffect, MobEffect> VULNERABILITY = EFFECTS.register("vulnerability_effect",
            () -> new ScaledAttributeEffect(MobEffectCategory.HARMFUL, 0x8B0000, ExAttributes.INJURY_FREE, Exmodifier.id("effect/vulnerability"), -0.02D, 255, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    public static final DeferredHolder<MobEffect, MobEffect> HIT_RATE = EFFECTS.register("hit_rate_effect",
            () -> new ScaledAttributeEffect(MobEffectCategory.BENEFICIAL, 0xE6C35C, ExAttributes.HIT_RATE, Exmodifier.id("effect/hit_rate"), 0.05D, 3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    public static final DeferredHolder<MobEffect, MobEffect> REDUCE_INJURIES = EFFECTS.register("reduce_injuries_effect",
            () -> new ScaledAttributeEffect(MobEffectCategory.BENEFICIAL, 0x6BCB77, ExAttributes.INJURY_FREE, Exmodifier.id("effect/reduce_injuries"), 0.02D, 5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("exmodifier", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.exmodifier"))
            .icon(() -> ENTRY_ITEM.get().getDefaultInstance())
            .displayItems((params, output) -> {
                output.accept(ENTRY_ITEM.get());
                output.accept(ENTRY_ITEM_EMPTY.get());
                output.accept(REFRESH_TABLE_ITEM.get());
                output.accept(EMBEDDED_TABLE_ITEM.get());
            }).build());

    private ExModifierContent() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        MENUS.register(bus);
        EFFECTS.register(bus);
        TABS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }

    /** Matches 1.20.1: bonus = min(amplifier, cap) * perLevel (amplifier 0 → 0). */
    private static final class ScaledAttributeEffect extends MobEffect {
        private final Holder<Attribute> attribute;
        private final ResourceLocation modifierId;
        private final double perLevel;
        private final int cap;
        private final AttributeModifier.Operation operation;

        private ScaledAttributeEffect(MobEffectCategory category, int color, Holder<Attribute> attribute,
                                      ResourceLocation modifierId, double perLevel, int cap, AttributeModifier.Operation operation) {
            super(category, color);
            this.attribute = attribute;
            this.modifierId = modifierId;
            this.perLevel = perLevel;
            this.cap = cap;
            this.operation = operation;
        }

        @Override
        public void createModifiers(int amplifier, BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
            double value = Math.min(amplifier, cap) * perLevel;
            if (value != 0.0D) {
                consumer.accept(attribute, new AttributeModifier(modifierId, value, operation));
            }
        }
    }
}
