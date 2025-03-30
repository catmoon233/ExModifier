package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.content.resources.ZipHandle;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ModListScreen;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.exmo.exmodifier.content.resources.ZipHandle.copyPacks;

@Mixin(ModListScreen.class)
public abstract class ModScreenMixin extends Screen {


    protected ModScreenMixin(Component p_96550_) {
        super(p_96550_);
    }

    @Override
    public void onFilesDrop(List<Path> p_100029_) {
        super.onFilesDrop(p_100029_);
        String s = (String)p_100029_.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
        this.minecraft.setScreen(new ConfirmScreen((p_280877_) -> {
            if (p_280877_) {
                copyPacks(this.minecraft, p_100029_, ZipHandle.ZIP_FILE_DIR);
            }

            this.minecraft.setScreen(this);
        }, Component.translatable("name.exmodifier.modmenu").append(Component.translatable("pack.dropConfirm")), Component.literal(s)));
    }
}
