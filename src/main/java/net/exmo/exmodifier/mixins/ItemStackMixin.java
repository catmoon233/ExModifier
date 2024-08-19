package net.exmo.exmodifier.mixins;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    private CompoundTag tag;
    /**
     * @author anmaos
     */
    @Inject(at = @At("RETURN"),method = "getAttributeModifiers",cancellable = true)
    private void nu$getAttributeModifiers$add(EquipmentSlot pSlot, CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir)
    {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        CompoundTag data = tag;
        if (data != null && data.contains("ExAttributeModifiers", 9)) {
            ListTag listtag = data.getList("ExAttributeModifiers", 10);
            for(int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                if (
                        !compoundtag.contains("Slot", 8)
                                || compoundtag.getString("Slot").equals(pSlot.getName())
                ) {
                    Attribute optional = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.tryParse(compoundtag.getString("AttributeName")));
                    if (optional != null) {
                        AttributeModifier attributemodifier = AttributeModifier.load(compoundtag);
                        if (attributemodifier != null && attributemodifier.getId().getLeastSignificantBits() != 0L && attributemodifier.getId().getMostSignificantBits() != 0L) {
                            multimap.put(optional, attributemodifier);
                        }
                    }
                }
            }
        }

        multimap.putAll(cir.getReturnValue());
       // Exmodifier.LOGGER.debug("getAttributeModifiers: " + pSlot + " " + multimap);
        cir.setReturnValue(multimap);
    }
}