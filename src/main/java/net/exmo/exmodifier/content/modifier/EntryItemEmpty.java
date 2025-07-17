package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.util.TooltipUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class EntryItemEmpty extends Item {
    public EntryItemEmpty(Properties p_41383_) {
        super(p_41383_);
    }


}