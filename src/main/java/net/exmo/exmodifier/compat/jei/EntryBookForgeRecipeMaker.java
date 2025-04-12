package net.exmo.exmodifier.compat.jei;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.jei.ScrollForgeRecipe;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.EntryItem;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.modifier.WashingMaterials;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * - Upgrade scroll: (scroll level x) + (scroll level x) = (scroll level x+1)
 * - Imbue Weapon:   weapon + scroll = imbued weapon with spell/level of scroll
 * - Upgrade item:   item + upgrade orb =
 **/
public final class EntryBookForgeRecipeMaker {
    private record FocusToSchool(Item item, SchoolType schoolType) {
        public FocusToSchool(Item item, SchoolType schoolType) {
            this.item = item;
            this.schoolType = schoolType;
        }
    }

    private EntryBookForgeRecipeMaker() {
        //private constructor prevents anyone from instantiating this class
    }

    public static List<EntryBookForgeRecipe> getRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        var material = ModifierHandle.materialsList;


        Stream<EntryBookForgeRecipe> entryBookForgeRecipeStream = Exmodifier.generateModifierItemStacks().stream()
                .map(item -> {


                    var modifierEntry = ModifierHandle.findModifierEntry(EntryItem.getModifierID(item));
                    var inputs = new ArrayList<ItemStack>();
                    var outputs = new ArrayList<ItemStack>();
//                    var inputs1 = new ArrayList<WashingMaterials>();
//                    var ints = new ArrayList<Integer>();

                    material.forEach(material1 -> {
                        var string = material1.ItemId;
                        if (modifierEntry.getModifierItemSelector().getOnlyWashItems().contains(string) || modifierEntry.getModifierItemSelector().getOnlyWashItems().isEmpty()) {
                            inputs.add(ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(string)).getDefaultInstance());
                            outputs.add(item);
//                            inputs1.add(material1);
//                            ints.add((int) material1.CostExp);
                        }

                    });

                    return new EntryBookForgeRecipe(inputs, outputs);

                });
        return entryBookForgeRecipeStream.toList();
    }

    private static ItemStack getScrollStack(AbstractSpell spell, int spellLevel) {
        var scrollStack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell, spellLevel, scrollStack);
        return scrollStack;
    }
}
