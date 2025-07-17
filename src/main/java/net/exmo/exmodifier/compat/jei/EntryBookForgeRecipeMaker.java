package net.exmo.exmodifier.compat.jei;





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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.materialsList;

/**
 * - Upgrade scroll: (scroll level x) + (scroll level x) = (scroll level x+1)
 * - Imbue Weapon:   weapon + scroll = imbued weapon with spell/level of scroll
 * - Upgrade item:   item + upgrade orb =
 **/
public final class EntryBookForgeRecipeMaker {


    private EntryBookForgeRecipeMaker() {
        //private constructor prevents anyone from instantiating this class
    }

    public static List<EntryBookForgeRecipe> getRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        var material = materialsList;


        Stream<EntryBookForgeRecipe> entryBookForgeRecipeStream = Exmodifier.generateModifierItemStacks().stream()
                .map(item -> {


                    var modifierEntry = ModifierHandle.findModifierEntry(EntryItem.getModifierID(item));
                    var inputs = new ArrayList<ItemStack>();
                    var outputs = new ArrayList<ItemStack>();
//                    var inputs1 = new ArrayList<WashingMaterials>();
//                    var ints = new ArrayList<Integer>();

                    material.forEach(material1 -> {
                        var string = material1.ItemId;
                            AtomicBoolean only = new AtomicBoolean(false);
                            boolean hasWashItem = materialsList.stream()
                                    .anyMatch(m -> {
                                        if ( m.ItemId.equals(material1.ItemId)){
                                            if (m.OnlyHasWashEntry) only.set(true);
                                            return true;
                                        }
                                        return false;
                                    });
                            boolean washPd = false;
                            if (modifierEntry.modifierItemSelector.getOnlyWashItems().isEmpty() ){
                                if (!only.get()) {
                                    washPd = true;
                                }
                            }else {
                                if (modifierEntry.modifierItemSelector.getOnlyWashItems().contains(material1.ItemId)){
                                    washPd = true;
                                }
                            }
                            if (washPd){
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


}
