package net.exmo.exmodifier.mixins;

import com.google.common.collect.Multimap;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ItemStack.class)
public abstract class ItemStackClientMixin {
    @Shadow public abstract boolean hasTag();

    @Unique
    private ChatFormatting exmodifier$chatFormatting = null;

    @SuppressWarnings("rawtypes")
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;getAmount()D"), method = "getTooltipLines", locals = LocalCapture.CAPTURE_FAILHARD)
    private void storeAttributeModifier(Player player, TooltipFlag context, CallbackInfoReturnable<List> cir, List list, MutableComponent component, int i , EquipmentSlot[] var6, int var7, int var8, EquipmentSlot equipmentSlot, Multimap multimap, Iterator var11, Map.Entry entry, AttributeModifier entityAttributeModifier) {
        ItemStack itemStack = (ItemStack) (Object) this;
            AtomicBoolean isSet = new AtomicBoolean(false);
        ModifierEntryHelper.of(itemStack).getModifierEntriesB().forEach(
                modifierEntry -> {
                    modifierEntry.attriGether.forEach(
                            attriGether -> {
                                if( entityAttributeModifier.getName().contains(attriGether.modifier.getName())){
                                    ChatFormatting chatFormattingFromString = ModifierHandle.getChatFormattingFromString(Component.translatable(modifierEntry.getDescriptionId()).getString());
                                    if (chatFormattingFromString == null) chatFormattingFromString = ChatFormatting.BLUE;
                                    exmodifier$chatFormatting = chatFormattingFromString;
                                    isSet.set(true);
                                };
                            }
                    );
                }
        );
        if (!isSet.get()) {
            exmodifier$chatFormatting = null;
        }

    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 5), method = "getTooltipLines")
    private MutableComponent getTextFormatting(MutableComponent translatableText, ChatFormatting formatting) {
        if(this.hasTag() && exmodifier$chatFormatting !=null) {

            return translatableText.withStyle(exmodifier$chatFormatting);
        } else {
            return translatableText.withStyle(formatting);
        }
    }
}
