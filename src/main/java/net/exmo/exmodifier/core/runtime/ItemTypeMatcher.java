package net.exmo.exmodifier.core.runtime;

import net.exmo.exmodifier.core.data.ItemTypeDefinition;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;

public final class ItemTypeMatcher {
    private ItemTypeMatcher() {}

    public static boolean matchesType(ItemStack stack, ResourceLocation typeId) {
        if (stack == null || stack.isEmpty() || typeId == null) return false;
        ItemTypeDefinition definition = ExModifierCatalog.current().itemTypes().get(typeId);
        if (definition == null) return false;
        return matches(stack, definition);
    }

    public static boolean matchesAnyType(ItemStack stack, Collection<ResourceLocation> typeIds) {
        if (typeIds == null || typeIds.isEmpty()) return true;
        for (ResourceLocation typeId : typeIds) {
            if (matchesType(stack, typeId)) return true;
        }
        return false;
    }

    public static boolean matches(ItemStack stack, List<ResourceLocation> onlyItems, List<String> onlyTags) {
        if (stack == null || stack.isEmpty()) return false;
        boolean itemsOk = onlyItems == null || onlyItems.isEmpty() || onlyItems.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (!itemsOk) return false;
        if (onlyTags == null || onlyTags.isEmpty()) return true;
        for (String tag : onlyTags) {
            if (tag == null || tag.isBlank()) continue;
            ResourceLocation id = ResourceLocation.tryParse(tag);
            if (id == null) continue;
            if (stack.is(TagKey.create(Registries.ITEM, id))) return true;
        }
        return false;
    }

    public static boolean matches(ItemStack stack, ItemTypeDefinition definition) {
        return definition != null && matches(stack, parse(definition.itemIds()), definition.itemTags());
    }

    private static List<ResourceLocation> parse(List<String> raw) {
        return raw.stream().map(ResourceLocation::tryParse).filter(id -> id != null).toList();
    }

    public static boolean isWeaponLike(ItemStack stack) {
        return matchesAnyType(stack, ExModifierCatalog.current().itemTypes().keySet())
                || stack.getItem() instanceof net.minecraft.world.item.SwordItem
                || stack.getItem() instanceof net.minecraft.world.item.ProjectileWeaponItem
                || stack.getItem() instanceof net.minecraft.world.item.AxeItem;
    }
}
