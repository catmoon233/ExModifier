package net.exmo.exmodifier.content.type;

import net.exmo.exmodifier.content.event.MainEvent;
import net.exmo.exmodifier.util.CuriosUtil;
import net.exmo.exmodifier.util.ItemSelector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.common.Tags;
import tfar.classicbar.impl.overlays.vanilla.Armor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExType {
    public ExType(String name,ItemSelector itemSelector,EquipmentSlot... equipmentSlots){
        this.name = name;
        this.itemSelector = new ArrayList<>(Collections.singleton(itemSelector));
        this.slot = equipmentSlots;
        ExTypeHandle.values.put(name,new ItemType(name, this.itemSelector,equipmentSlots));
    }
    public ExType addItemSelector(ItemSelector itemSelector){
        this.itemSelector.add(itemSelector);
        return this;
    }
    public ExType build(){
        ExTypeHandle.values.put(name,new ItemType(name, this.itemSelector,this.slot));
        return this;
    }

    public static ExType CURIOS = new ExType("CURIOS",new ItemSelector(((itemStack, atomicBoolean) -> {
        if (CuriosUtil.isCuriosItem(itemStack)){
            atomicBoolean.set(true);
        }
    })));
    public static ExType ALL = new ExType("ALL",new ItemSelector(
            (itemStack, atomicBoolean) -> {
               if( MainEvent.CommonEvent.hasAttrOrBow(itemStack)) atomicBoolean.set(true);
            }
    ));
    public static ExType UNKNOWN = new ExType("UNKNOWN",new ItemSelector());
    public static ExType ATTACKABLE = new ExType("ATTACKABLE",new ItemSelector(
            (itemStack, atomicBoolean) -> {
                if ( itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream()
                        .mapToDouble(AttributeModifier::getAmount).sum() >0)
                    atomicBoolean.set(true);
            }
    ),EquipmentSlot.MAINHAND);
    public static ExType ARMOR = new ExType("ARMOR",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
            Tags.Items.ARMORS,
            Tags.Items.ARMORS_LEGGINGS,
            Tags.Items.ARMORS_CHESTPLATES,
            Tags.Items.ARMORS_BOOTS,
            Tags.Items.ARMORS_HELMETS
    )

    )).addItemSelector(
            new ItemSelector(
                    (e ,v)->{
                        if (e.getItem() instanceof ArmorItem) v.set(true);
                    }
            )
    ).build();
    public static ExType WEAPON = new ExType("WEAPON",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(

            )
    ),EquipmentSlot.MAINHAND);
    public static ExType HELMET = new ExType("HELMET",
            new ItemSelector(null,null,null, ItemSelector.CompareType.TAG, List.of(
            Tags.Items.ARMORS_HELMETS
    )
    ));
    public static ExType CHESTPLATE = new ExType("CHESTPLATE",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
                    Tags.Items.ARMORS_CHESTPLATES
    )
    ),EquipmentSlot.CHEST);
    public static ExType LEGGINGS = new ExType("LEGGINGS",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
                    Tags.Items.ARMORS_LEGGINGS
    )
    ),EquipmentSlot.LEGS
    );
    public static ExType BOOTS = new ExType("BOOTS",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
                    Tags.Items.ARMORS_BOOTS
    )
    ),EquipmentSlot.FEET
    );
    public static ExType TOOL = new ExType("TOOL",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
                    Tags.Items.TOOLS
    )
        ),EquipmentSlot.MAINHAND
    );

    public static ExType FISHING_ROD = new ExType("FISHING_ROD",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
            Tags.Items.TOOLS_FISHING_RODS
    )),EquipmentSlot.MAINHAND
    );
    public static ExType TRIDENT = new ExType("TRIDENT",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
                    Tags.Items.TOOLS_TRIDENTS
    )
    ),EquipmentSlot.MAINHAND
    );
    public static ExType CROSSBOW = new ExType("CROSSBOW",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
                    Tags.Items.TOOLS_CROSSBOWS
    )
    ),EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND
    );
    public static ExType BOW = new ExType("BOW",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
                    Tags.Items.TOOLS_BOWS
    )
    ),EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND
    );
    public static ExType SHIELD = new ExType("SHIELD",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
                    Tags.Items.TOOLS_SHIELDS
    )
    ),EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND
    );
    public static ExType PICKAXE = new ExType("PICKAXE",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
            ItemTags.create(new ResourceLocation("minecraft", "pickaxes"))
            )
    ),EquipmentSlot.MAINHAND
    );
    public static ExType AXE = new ExType("AXE",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
            ItemTags.create(new ResourceLocation("minecraft", "axes"))
            )
    ),EquipmentSlot.MAINHAND
    ).addItemSelector(
            new ItemSelector(
                    (e ,v)->{
                        if (e.getItem() instanceof AxeItem) v.set(true);
                    }
            )
    ).build();
    public static ExType SHOVEL = new ExType("SHOVEL",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
                    ItemTags.create(new ResourceLocation("minecraft", "shovels"))
            )
    ),EquipmentSlot.MAINHAND
    ).addItemSelector(
            new ItemSelector(
                    (e ,v)->{
                        if (e.getItem() instanceof ShovelItem) v.set(true);
                    }
            )
    ).build();
    public static ExType HOE = new ExType("HOE",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
                    ItemTags.create(new ResourceLocation("minecraft", "hoes"))
            )
    ),EquipmentSlot.MAINHAND
    ).addItemSelector(
            new ItemSelector(
                    (e ,v)->{
                        if (e.getItem() instanceof HoeItem) v.set(true);
                    }
            )
    ).build();;
    public static ExType SWORD = new ExType("SWORD",new ItemSelector(
            null,null,null, ItemSelector.CompareType.TAG, List.of(
                    ItemTags.create(new ResourceLocation("minecraft", "swords"))
            )
    ),EquipmentSlot.MAINHAND
    ).addItemSelector(
            new ItemSelector(
                    (e ,v)->{
                        if (e.getItem() instanceof SwordItem) v.set(true);
                    }
            )
    ).build();;


    public static ExType HAND = new ExType("HAND",new ItemSelector());
    public static ExType OFFHAND = new ExType("OFFHAND",new ItemSelector(
            (itemStack, atomicBoolean) -> {
                if (itemStack.getEquipmentSlot() == EquipmentSlot.OFFHAND){
                    atomicBoolean.set(true);
                }
            }
    ));
    public static ExType MAINHAND = new ExType("MAINHAND",new ItemSelector(
            (itemStack, atomicBoolean) -> {
                if (itemStack.getEquipmentSlot() == EquipmentSlot.MAINHAND){
                    atomicBoolean.set(true);
                }
            }
    ));
    public final String name;
    public final ArrayList<ItemSelector> itemSelector;
    public final EquipmentSlot[] slot;


    public ItemType get(){
        return ExTypeHandle.values.get(name);
    }
    @Override 
    public String toString() {
                 return name;
             }
}

