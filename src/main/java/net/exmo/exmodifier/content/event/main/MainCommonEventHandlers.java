package net.exmo.exmodifier.content.event.main;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.event.MainEvent;
import net.exmo.exmodifier.events.ExAfterArmorChange;
import net.exmo.exmodifier.events.ExApplyEntryAttrigetherEvent;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.util.DynamicExpressionEvaluator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import javax.script.ScriptException;
import java.io.IOException;

@Mod.EventBusSubscriber
public final class MainCommonEventHandlers {
    private MainCommonEventHandlers() {
    }

    @SubscribeEvent
    public static void curiosChange(CurioChangeEvent event) {
        MainArmorService.handleCuriosChange(event);
    }

    @SubscribeEvent
    public static void iLevelAttriGetherModifier(ExApplyEntryAttrigetherEvent event) {
        if (event.attriGether.Expression == null || event.attriGether.Expression.isEmpty()) {
            return;
        }

        int level = event.modifierInstant.getLevel();
        Exmodifier.LOGGER.debug("iLevelAttriGetherModifier: " + event.attriGether.Expression + " level: " + level);
        DynamicExpressionEvaluator dynamicExpressionEvaluator = new DynamicExpressionEvaluator();
        dynamicExpressionEvaluator.setVariable("level", level);
        dynamicExpressionEvaluator.setVariable("l", level);
        double amount = dynamicExpressionEvaluator.evaluate(event.attriGether.Expression);
        event.attriGether.modifier.setAmount(amount);
    }

    @SubscribeEvent
    public static void atJoinGame(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        MainSuitService.rebuildSuitState(player);
    }

    @SubscribeEvent
    public static void grind(GrindstoneEvent.OnPlaceItem event) {
        MainArmorService.handleArmorChangeExpectSuit(event.getOutput(), false);
    }

    @SubscribeEvent
    public static void armorChange(LivingEquipmentChangeEvent event) throws ScriptException {
        if (event.getEntity() instanceof Player player) {
            boolean operated = MainArmorService.handleArmorChange(player, event.getFrom(), event.getTo(), event.getEntity().level().isClientSide());
            MinecraftForge.EVENT_BUS.post(new ExAfterArmorChange(event, operated));
        }
    }

    @SubscribeEvent
    public static void atReload(AddReloadListenerEvent event) throws IOException {
        MainReloadService.registerReloadListeners(event);
    }

    @SubscribeEvent
    public static void playJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer) || serverPlayer.level().isClientSide()) {
            return;
        }

        MainEvent.DataCache dataCache = MainEvent.createDataCache();
        ModifierHandle.sendClearDataToClient(serverPlayer);
        MainEvent.sendExmoServerDataToServerPlayer(serverPlayer, dataCache);
    }
}
