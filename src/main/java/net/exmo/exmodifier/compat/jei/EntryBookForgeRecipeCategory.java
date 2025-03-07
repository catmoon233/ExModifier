package net.exmo.exmodifier.compat.jei;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.jei.AlchemistCauldronJeiRecipe;
import io.redspace.ironsspellbooks.jei.JeiPlugin;
import io.redspace.ironsspellbooks.jei.ScrollForgeRecipe;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.EntryItem;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.modifier.WashingMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class EntryBookForgeRecipeCategory implements IRecipeCategory<EntryBookForgeRecipe> {
    public static final RecipeType<EntryBookForgeRecipe> ENTRY_BOOK_FORGE_RECIPE_TYPE = RecipeType.create(Exmodifier.MODID, "entry_book_forge", EntryBookForgeRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final String refresh_items = "refresh_items";
    private final String exp = "exp";
    private final String entry_output = "entry_output";
    private final String washingMaterials = "washingMaterials";


    public EntryBookForgeRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = new ResourceLocation("exmodifier", "textures/gui/jei_forge.png");
        background = guiHelper.drawableBuilder(location,  0, 0, 125, 18)
                .addPadding(0, 0, 0, 0)
                .build();
        icon = guiHelper.createDrawableItemStack(new ItemStack(Exmodifier.ENTRY_ITEM.get()));
    }

    @Override
    public RecipeType<EntryBookForgeRecipe> getRecipeType() {
        return ENTRY_BOOK_FORGE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("itemGroup.exmodifier_tab");
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
    public void setRecipe(IRecipeLayoutBuilder builder, EntryBookForgeRecipe recipe, IFocusGroup focuses) {
        var materials = recipe.refresh_items();
        var output = recipe.entry_output();


        IRecipeSlotBuilder materialSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStacks(materials)
                .setSlotName(refresh_items);

//
//        IRecipeSlotBuilder paperInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 24, 1)
//                .addItemStack(paperInput)
//                .setSlotName(exp);

//        IRecipeSlotBuilder focusInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 47, 1)
//                .addItemStack(focusInput)
//                .setSlotName(focusSlotName);


        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 1)
                .addItemStacks(output)
                .setSlotName(entry_output);
        builder.createFocusLink(materialSlot, outputSlot);
    }

    @Override
    public void draw(EntryBookForgeRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Optional<ItemStack> leftStack = recipeSlotsView.findSlotByName(refresh_items)
                .flatMap(IRecipeSlotView::getDisplayedItemStack);
        Optional<ItemStack> right = recipeSlotsView.findSlotByName(entry_output)
                .flatMap(IRecipeSlotView::getDisplayedItemStack);
        if (leftStack.isPresent() && right.isPresent()
        ) {
            List<WashingMaterials> list = ModifierHandle.materialsList
                    .stream()
                    .filter(entry -> entry.ItemId.equals(ForgeRegistries.ITEMS.getKey(leftStack.get().getItem()).toString())).toList();
            if (!list.isEmpty()) {

                WashingMaterials washingMaterials1 = list.get(0);
                var inputText = Component.translatable("exmodifier.container.refresh.cost", washingMaterials1.CostExp);

                var font = Minecraft.getInstance().font;
                int y = (getHeight() / 2)-1;
                int x = (getWidth() - font.width(inputText)) * 3 / 4;
                guiGraphics.drawString(font, inputText, x, y, Color.green.getRGB());
                guiGraphics.drawString(font, Component.translatable("exmodifier.container.refresh.cost.chance",EntryItem.CommonEvent.df.format(EntryItem.getModifierChance(right.get()))), x, y-8, Color.CYAN.getRGB());
                guiGraphics.drawString(font, Component.translatable("exmodifier.container.refresh.cost.rarity",washingMaterials1.rarity), x, y+8, Color.magenta.getRGB());
                guiGraphics.drawString(font, Component.translatable("exmodifier.container.refresh.cost.needCount",washingMaterials1.NeedCount), x, y+16, Color.yellow.getRGB());

            }
        }
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }
}