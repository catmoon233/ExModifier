package net.exmo.exmodifier.mixins;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.content.helper.ItemQualityHelper;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static net.exmo.exmodifier.Config.refresh_time;


@Mixin(ItemStack.class)
public abstract class ItemStackMixin  {

    @Shadow
    private CompoundTag tag;

    @Shadow public abstract boolean hasTag();

    @Shadow @Nullable public abstract CompoundTag getTagElement(String p_41738_);

    /**
     * @author anmaos
     */
    @Inject(at = @At("RETURN"),method = "getAttributeModifiers",cancellable = true)
    private void nu$getAttributeModifiers$add(EquipmentSlot pSlot, CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir)
    {
        if (((ItemStack)(Object) this).getTag()==null)return;
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
                        //attributemodifier = new AttributeModifier(UUID.randomUUID(),attributemodifier.getName(), attributemodifier.getAmount(), attributemodifier.getOperation());
                    //    Exmodifier.LOGGER.debug(attributemodifier.getName(), attributemodifier.getId(), attributemodifier.getOperation());
                        if (attributemodifier != null && attributemodifier.getId().getLeastSignificantBits() != 0L && attributemodifier.getId().getMostSignificantBits() != 0L) {
                            multimap.put(optional, attributemodifier);
                        }
                    }
                }
            }
        }
        multimap.putAll(cir.getReturnValue());
        cir.setReturnValue(multimap);
    }
//    @Inject(at = @At("TAIL"),method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/nbt/CompoundTag;)V")
//    private void nu$init$add(ItemLike p_41604_, int p_41605_, CompoundTag p_41606_, CallbackInfo ci)
//    {
//        ItemStack instance = (ItemStack)(Object) this;
//
//    }
@Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
private void onGetDisplayName(CallbackInfoReturnable<Component> cir) {
        ItemStack stack = (ItemStack) (Object) this;
    Component component = cir.getReturnValue();
    for (var q : ItemQualityHelper.of(stack).getQualityEntriesTooltip()) {
        if (q.isShowInHeadTooltip) {
            component = Component.translatable(q.mutableComponent.getString()).append(" ").append(component);

        }

    }
    for (var m : ModifierEntryHelper.of(stack).getModifierEntriesB()){
        if (m.displayNameInItemName || Config.ADMNUIN) component  =(Component.translatable(m.getDescriptionId()).append(" ").append(component));
    }
    cir.setReturnValue(component);
}


}