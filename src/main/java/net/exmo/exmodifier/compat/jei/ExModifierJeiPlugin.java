package net.exmo.exmodifier.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.api.ExModifierApi;
import net.exmo.exmodifier.content.ExModifierContent;
import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.exmo.exmodifier.core.runtime.ModifierService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public final class ExModifierJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return Exmodifier.id("jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ExModifierContent.ENTRY_ITEM.get(), (stack, context) ->
                ExModifierApi.entryBook(stack).map(book -> book.entryId().toString() + "/" + book.level()).orElse(""));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new RefreshRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RefreshRecipe> recipes = new ArrayList<>();
        List<ItemStack> books = allBooks();
        ExModifierCatalog.current().washingMaterials().forEach((id, definition) -> {
            ItemStack material = new ItemStack(BuiltInRegistries.ITEM.get(id));
            if (material.isEmpty() || books.isEmpty()) return;
            recipes.add(new RefreshRecipe(List.of(material), books));
        });
        registration.addRecipes(RefreshRecipeCategory.TYPE, recipes.stream().filter(RefreshRecipe::valid).toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ExModifierContent.REFRESH_TABLE_ITEM.get()), RefreshRecipeCategory.TYPE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        List<ItemStack> books = allBooks();
        if (!books.isEmpty()) {
            jeiRuntime.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, books);
        }
    }

    private static List<ItemStack> allBooks() {
        List<ItemStack> books = new ArrayList<>();
        ExModifierCatalog.current().entries().forEach((id, definition) -> {
            int max = Math.max(1, definition.maxLevel());
            for (int level = 1; level <= max; level++) {
                books.add(ModifierService.copyBook(id, level));
            }
        });
        return books;
    }
}
