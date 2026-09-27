package net.exmo.exmodifier.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Optional Curios scan. Never class-loads Curios types unless the mod is present. */
public final class CuriosEquipment {
    private CuriosEquipment() {}

    public static boolean available() {
        return ModList.get().isLoaded("curios");
    }

    public static List<ItemStack> equipped(LivingEntity entity) {
        if (entity == null || !available()) return List.of();
        try {
            Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method inventory = find(api, "getCuriosInventory", LivingEntity.class);
            if (inventory == null) return List.of();
            Object result = inventory.invoke(null, entity);
            Object handler = unwrap(result);
            if (handler == null) return List.of();
            Object stacks = invoke(handler, "getEquippedCurios");
            if (stacks == null) stacks = invoke(handler, "getWornStacks");
            if (stacks == null) return fromSlotMap(handler);
            return fromStackHandler(stacks);
        } catch (Throwable ignored) {
            return List.of();
        }
    }

    private static List<ItemStack> fromSlotMap(Object handler) throws Exception {
        Object curios = invoke(handler, "getCurios");
        if (!(curios instanceof java.util.Map<?, ?> map)) return List.of();
        List<ItemStack> stacks = new ArrayList<>();
        for (Object value : map.values()) {
            Object inner = invoke(value, "getStacks");
            stacks.addAll(fromStackHandler(inner));
        }
        return stacks;
    }

    private static List<ItemStack> fromStackHandler(Object stacks) throws Exception {
        if (stacks == null) return List.of();
        Object slotCount = invoke(stacks, "getSlots");
        if (!(slotCount instanceof Integer count)) return List.of();
        Method get = stacks.getClass().getMethod("getStackInSlot", int.class);
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Object stack = get.invoke(stacks, i);
            if (stack instanceof ItemStack item && !item.isEmpty()) list.add(item);
        }
        return list;
    }

    private static Object unwrap(Object value) {
        if (value instanceof Optional<?> optional) return optional.orElse(null);
        return value;
    }

    private static Object invoke(Object target, String name) {
        if (target == null) return null;
        try {
            return target.getClass().getMethod(name).invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Method find(Class<?> type, String name, Class<?>... args) {
        try {
            return type.getMethod(name, args);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
