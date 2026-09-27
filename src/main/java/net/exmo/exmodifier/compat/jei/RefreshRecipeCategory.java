package net.exmo.exmodifier.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.ExModifierContent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class RefreshRecipeCategory implements IRecipeCategory<RefreshRecipe> {
    public static final RecipeType<RefreshRecipe> TYPE = RecipeType.create(Exmodifier.MODID, "refresh", RefreshRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final Component title = Component.translatable("jei.exmodifier.refresh");

    public RefreshRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(116, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ExModifierContent.REFRESH_TABLE_ITEM.get()));
    }

    @Override
    public RecipeType<RefreshRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RefreshRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19).addItemStacks(recipe.materials());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19).addItemStacks(recipe.outputs());
    }
}
