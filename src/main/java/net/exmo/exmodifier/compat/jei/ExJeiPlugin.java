package net.exmo.exmodifier.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IIngredientManager;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.EntryItem;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.type.ExTypeHandle;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.init.RegisterOther;
import net.exmo.exmodifier.util.ExUtil;
import net.exmo.exmodifier.util.TooltipUtil;
import net.exmo.exmodifier.util.WeightedUtil;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.Exmodifier.ENTRY_ITEM;
import static net.exmo.exmodifier.content.modifier.ModifierHandle.modifierEntryMap;

@JeiPlugin
public class ExJeiPlugin implements IModPlugin {


    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IIngredientManager ingredientManager = registration.getIngredientManager();
        IVanillaRecipeFactory vanillaRecipeFactory = registration.getVanillaRecipeFactory();

        Map<String, WeightedUtil<String>> weights = new HashMap<>();


        for (ItemType type : ExTypeHandle.itemTypes.values()) {
            weights.put(type.name(), new WeightedUtil<>(modifierEntryMap.entrySet().stream().filter(e -> {
                return e.getValue().types.contains(type);
            }).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().weight))));
        }

        modifierEntryMap.forEach((entry, modifierEntry) -> {
            ItemStack stack = ENTRY_ITEM.get().getDefaultInstance();
            stack.getOrCreateTag().putString("modifier_id", entry);
            ListTag listTag = new ListTag();
            for (ItemType type : modifierEntry.types){
                listTag.add(StringTag.valueOf(type.name()));
            }
            stack.getOrCreateTag().put("modifier_types", listTag);
            double probability = modifierEntry.types.stream().mapToDouble(type -> weights.get(type.name()).getProbability(entry)).sum();
          //  double probability = exElement.types.stream().mapToDouble(type -> weights.get(type.name()).getProbability(entry) / totalWeight).sum();
            stack.getOrCreateTag().putDouble("modifier_possibility", probability);
            if (modifierEntry.maxLevel <= 1) {
                stack.getOrCreateTag().putInt("modifier_level", 1);
                generateTooltipComponents(stack, registration);
            } else {
                for (int i = 1; i <= modifierEntry.maxLevel; i++) {
                    ItemStack stack1 = stack.copy();
                    stack1.getOrCreateTag().putInt("modifier_level", i);
                    generateTooltipComponents(stack1, registration);
                }
            }
        });

        registration.addRecipes(EntryBookForgeRecipeCategory.ENTRY_BOOK_FORGE_RECIPE_TYPE, EntryBookForgeRecipeMaker.getRecipes(vanillaRecipeFactory, ingredientManager));

    }


    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Exmodifier.MODID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ENTRY_ITEM.get(),(stack, context) -> stack.getOrCreateTag().getString("modifier_id"));
        IModPlugin.super.registerItemSubtypes(registration);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        IModPlugin.super.registerIngredients(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(RegisterOther.BlockAbout.REFRESH_TABLE.get()), EntryBookForgeRecipeCategory.ENTRY_BOOK_FORGE_RECIPE_TYPE);

        IModPlugin.super.registerRecipeCatalysts(registration);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registration.addRecipeCategories(new EntryBookForgeRecipeCategory(guiHelper));
        IModPlugin.super.registerCategories(registration);
    }

    // 新增方法：生成物品的 Tooltip 组件
    private void generateTooltipComponents(ItemStack stack, IRecipeRegistration registration) {
        List<Component> lc = new ArrayList<>();
        String modifierId = EntryItem.getModifierID(stack);
        ModifierEntry modifierEntry = modifierEntryMap.get(modifierId);
        if (modifierEntry == null) {
            lc.add(Component.translatable("modifier.entry.unknown_modifier"));
            return;
        }
        if (modifierId.length() <= 2) return;
        lc.add(Component.translatable(modifierEntry.getDescriptionId()));
        lc.add(Component.translatable("modifier.entry.possibility").append(EntryItem.CommonEvent.df.format(stack.getTag().getDouble("modifier_possibility") * 100)).append("%"));
        lc.add(Component.translatable("modifier.entry.level").append(String.valueOf(EntryItem.getModifierLevel(stack))));
        lc.add(Component.translatable("modifier.entry.maxlevel").append(String.valueOf(modifierEntry.maxLevel)));

        if (!modifierEntry.localDescription.isEmpty()) lc.addAll(TooltipUtil.sprit(Component.translatable("modifier.entry.desc").append(Component.translatable(modifierEntry.localDescription))));
        if (!modifierEntry.Slots.isEmpty()) {
            if (modifierEntry.Slots.size() == 1) {
                lc.add(Component.translatable("modifier.entry.slot").append(Component.translatable("modifier.slot." + modifierEntry.Slots.get(0))));
            } else {
                lc.add(Component.translatable("modifier.entry.slot"));
                for (var slot : modifierEntry.Slots) {
                    lc.add(Component.literal(" §7¦ §r").append(Component.translatable("modifier.slot." + slot)));
                }
            }
        }
        String modifierType = stack.getTag().getString("modifier_type");
        if (!modifierType.isEmpty())
            lc.add(Component.translatable("modifier.entry.type").append(Component.translatable(modifierType)));
        lc.add(Component.literal(" "));
        lc.addAll(modifierEntry.GenerateItemTooltip());
        registration.addItemStackInfo(stack, lc.toArray(new Component[0]));
    }
}