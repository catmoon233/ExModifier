package net.exmo.exmodifier.network;


import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.attributeEffect.AttriGetherEffectInstance;
import net.exmo.exmodifier.content.suit.ExSuit;
import net.exmo.exmodifier.content.suit.ExSuitHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ExModifiervaV {
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        Exmodifier.addNetworkMessage(SavedDataSyncMessage.class, SavedDataSyncMessage::buffer, SavedDataSyncMessage::new, SavedDataSyncMessage::handler);
        Exmodifier.addNetworkMessage(PlayerVariablesSyncMessage.class, PlayerVariablesSyncMessage::buffer, PlayerVariablesSyncMessage::new, PlayerVariablesSyncMessage::handler);
    }

    @SubscribeEvent
    public static void init(RegisterCapabilitiesEvent event) {
        event.register(PlayerVariables.class);
    }

    @Mod.EventBusSubscriber
    public static class EventBusVariableHandlers {
        @SubscribeEvent
        public static void onPlayerLoggedInSyncPlayerVariables(PlayerEvent.PlayerLoggedInEvent event) {
            if (!event.getEntity().level().isClientSide())
                ((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
        }

        @SubscribeEvent
        public static void onPlayerRespawnedSyncPlayerVariables(PlayerEvent.PlayerRespawnEvent event) {
            if (!event.getEntity().level().isClientSide())
                ((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
        }

        @SubscribeEvent
        public static void onPlayerChangedDimensionSyncPlayerVariables(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (!event.getEntity().level().isClientSide())
                ((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
        }

        @SubscribeEvent
        public static void clonePlayer(PlayerEvent.Clone event) {
            event.getOriginal().revive();
            PlayerVariables original = ((PlayerVariables) event.getOriginal().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
            PlayerVariables clone = ((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
            clone.Sitemstack = original.Sitemstack;
            clone.Suits = original.Suits;
            if (!event.isWasDeath())
            {
                clone.craftIndex = original.craftIndex;
                clone.syncContent = original.syncContent;
                clone.attriGetherEffectInstances = original.attriGetherEffectInstances;
                clone.itemsDamage = original.itemsDamage;
                clone.SuitsNum = original.SuitsNum;
            }

        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (!event.getEntity().level().isClientSide()) {
                SavedData mapdata = MapVariables.get(event.getEntity().level());
                SavedData worlddata = WorldVariables.get(event.getEntity().level());
                if (mapdata != null)
                    Exmodifier.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), new SavedDataSyncMessage(0, mapdata));
                if (worlddata != null)
                    Exmodifier.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), new SavedDataSyncMessage(1, worlddata));
            }
        }

        @SubscribeEvent
        public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (!event.getEntity().level().isClientSide()) {
                SavedData worlddata = WorldVariables.get(event.getEntity().level());
                if (worlddata != null)
                    Exmodifier.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), new SavedDataSyncMessage(1, worlddata));
            }
        }
    }

    public static class WorldVariables extends SavedData {
        public static final String DATA_NAME = "exmodifier_worldvars";

        public static WorldVariables load(CompoundTag tag) {
            WorldVariables data = new WorldVariables();
            data.read(tag);
            return data;
        }

        public void read(CompoundTag nbt) {
        }

        @Override
        public CompoundTag save(CompoundTag nbt) {
            return nbt;
        }

        public void syncData(LevelAccessor world) {
            this.setDirty();
            if (world instanceof Level level && !level.isClientSide())
                Exmodifier.PACKET_HANDLER.send(PacketDistributor.DIMENSION.with(level::dimension), new SavedDataSyncMessage(1, this));
        }

        static WorldVariables clientSide = new WorldVariables();

        public static WorldVariables get(LevelAccessor world) {
            if (world instanceof ServerLevel level) {
                return level.getDataStorage().computeIfAbsent(e -> WorldVariables.load(e), WorldVariables::new, DATA_NAME);
            } else {
                return clientSide;
            }
        }
    }

    public static class MapVariables extends SavedData {
        public static final String DATA_NAME = "exmodifier_mapvars";
        public static List<Map.Entry<ServerBossEvent, UUID>> bossBarList = new ArrayList<>();

        private @NotNull CompoundTag getBossBarCompoundTag(ServerBossEvent bossEvent) {
        CompoundTag bossEventNBT = new CompoundTag();
        bossEventNBT.putString("Name", bossEvent.getName().getString());
        bossEventNBT.putString("Color", bossEvent.getColor().getName());
        ServerPlayer serverPlayer = bossEvent.getPlayers().stream().findFirst().orElse(null);
        if (serverPlayer != null) bossEventNBT.putString("Player", serverPlayer.getName().getString());
        bossEventNBT.putString("BossBarOverlay", bossEvent.getOverlay().getName());
        bossEventNBT.putString("Player", bossEvent.getOverlay().getName());
        bossEventNBT.putBoolean("IsVisible", bossEvent.isVisible());
        bossEventNBT.putFloat("Progress", bossEvent.getProgress());
        return bossEventNBT;
    }
    private ServerBossEvent getBossBarFromNBT(CompoundTag bossEventNBT) {
            ServerBossEvent bossEvent = null;
            Component name = Component.translatable(bossEventNBT.getString("Name"));
            int progress = bossEventNBT.getInt("Progress");
            BossEvent.BossBarOverlay overlay = BossEvent.BossBarOverlay.valueOf(bossEventNBT.getString("BossBarOverlay"));

            //ServerPlayer serverPlayer = (bossEventNBT.getString("Player"));
            BossEvent.BossBarColor color = BossEvent.BossBarColor.valueOf(bossEventNBT.getString("Color"));
            bossEvent = new ServerBossEvent(name, color, overlay);
            bossEvent.setProgress(progress);
            bossEvent.setVisible(bossEventNBT.getBoolean("IsVisible"));
           // bossEvent.addPlayer(serverPlayer);
        return bossEvent;


    }

        public static MapVariables load(CompoundTag tag) {
            MapVariables data = new MapVariables();
            data.read(tag);
            return data;
        }

        public void read(CompoundTag nbt) {

        }

        @Override
        public CompoundTag save(CompoundTag nbt) {

            return nbt;
        }

        public void syncData(LevelAccessor world) {
            this.setDirty();
            if (world instanceof Level && !world.isClientSide())
                Exmodifier.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(), new SavedDataSyncMessage(0, this));
        }

        static MapVariables clientSide = new MapVariables();

        public static MapVariables get(LevelAccessor world) {
            if (world instanceof ServerLevelAccessor serverLevelAcc) {
                return serverLevelAcc.getLevel().getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(e -> MapVariables.load(e), MapVariables::new, DATA_NAME);
            } else {
                return clientSide;
            }
        }
    }

    public static class SavedDataSyncMessage {
        public int type;
        public SavedData data;

        public SavedDataSyncMessage(FriendlyByteBuf buffer) {
            this.type = buffer.readInt();
            this.data = this.type == 0 ? new MapVariables() : new WorldVariables();
            if (this.data instanceof MapVariables _mapvars)
                _mapvars.read(buffer.readNbt());
            else if (this.data instanceof WorldVariables _worldvars)
                _worldvars.read(buffer.readNbt());
        }

        public SavedDataSyncMessage(int type, SavedData data) {
            this.type = type;
            this.data = data;
        }

        public static void buffer(SavedDataSyncMessage message, FriendlyByteBuf buffer) {
            buffer.writeInt(message.type);
            buffer.writeNbt(message.data.save(new CompoundTag()));
        }

        public static void handler(SavedDataSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (!context.getDirection().getReceptionSide().isServer()) {
                    if (message.type == 0)
                        MapVariables.clientSide = (MapVariables) message.data;
                    else
                        WorldVariables.clientSide = (WorldVariables) message.data;
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final Capability<PlayerVariables> PLAYER_VARIABLES_CAPABILITY = CapabilityManager.get(new CapabilityToken<PlayerVariables>() {
    });

    @Mod.EventBusSubscriber
    private static class PlayerVariablesProvider implements ICapabilitySerializable<Tag> {
        @SubscribeEvent
        public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer))
                event.addCapability(new ResourceLocation("exmodifier", "player_variables"), new PlayerVariablesProvider());
        }

        private final PlayerVariables playerVariables = new PlayerVariables();
        private final LazyOptional<PlayerVariables> instance = LazyOptional.of(() -> playerVariables);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap == PLAYER_VARIABLES_CAPABILITY ? instance.cast() : LazyOptional.empty();
        }

        @Override
        public Tag serializeNBT() {
            return playerVariables.writeNBT();
        }

        @Override
        public void deserializeNBT(Tag nbt) {
            playerVariables.readNBT(nbt);
        }
    }

    public static class PlayerVariables {
        public  List<ExSuit> Suits = new ArrayList<>();
        public Map<ExSuit, Integer> SuitsNum = new HashMap<>();
        public Map<String, Float> itemsDamage = new HashMap<>();
        public Map<String,String> syncContent = new HashMap<>();
        public List<AttriGetherEffectInstance> attriGetherEffectInstances = new ArrayList<>();
        public ItemStack Sitemstack = ItemStack.EMPTY;
        public int craftIndex = -2;

        public void syncPlayerVariables(Entity entity) {
            if (entity instanceof ServerPlayer serverPlayer)
                Exmodifier.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new PlayerVariablesSyncMessage(this));
        }

        public Tag writeNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("craftIndex", craftIndex);
            nbt.put("Sitemstack", Sitemstack.save(new CompoundTag()));
            ListTag ExsuitListTag = new ListTag();
            for (ExSuit value : Suits) {
                if (value != null) {
                    ExsuitListTag.add(StringTag.valueOf(value.id));
                }
            }
            nbt.put("Suits", ExsuitListTag);

            ListTag EFListTag = new ListTag();
            for (AttriGetherEffectInstance value : attriGetherEffectInstances) {
                if (value != null) {
                    EFListTag.add(value.toNBT());
                }
            }
            nbt.put("attriGetherEffectInstances", EFListTag);

            CompoundTag SuitsNuma = new CompoundTag();
            for (Map.Entry<ExSuit, Integer> entry : SuitsNum.entrySet()) {
                if (entry==null){
                    continue;
                }
                if (entry.getKey()==null){
                    continue;
                }
                SuitsNuma.putString(entry.getKey().id, entry.getValue().toString());
            }
            nbt.put("SuitsNum", SuitsNuma);

            CompoundTag AddDamageTags = new CompoundTag();
            for (Map.Entry<String, Float> entry : itemsDamage.entrySet()) {
                AddDamageTags.putString(entry.getKey(), entry.getValue().toString());
            }
            nbt.put("itemsDamage", AddDamageTags);

            CompoundTag SyncContentTags = new CompoundTag();
            for (Map.Entry<String, String> entry : syncContent.entrySet()) {
                SyncContentTags.putString(entry.getKey(), entry.getValue());
            }
            nbt.put("syncContent", SyncContentTags);
            return nbt;
        }

        public void readNBT(Tag Tag) {
            CompoundTag nbt = (CompoundTag) Tag;
            craftIndex = nbt.getInt("craftIndex");
            Sitemstack = ItemStack.of(nbt.getCompound("Sitemstack"));
            ListTag ExSuitListTag = nbt.getList("Suits", 8);
            List<ExSuit> SuitsList = new ArrayList<>();
            for (int i = 0; i < ExSuitListTag.size(); ++i) {
                SuitsList.add(ExSuitHandle.LoadExSuit.get(ExSuitListTag.getString(i)));
            }
            Suits = SuitsList;

            ListTag EFListTag = nbt.getList("attriGetherEffectInstances", 10);
            List<AttriGetherEffectInstance> EFList = new ArrayList<>();
            for (int i = 0; i < EFListTag.size(); ++i) {
                EFList.add(AttriGetherEffectInstance.fromNBT(EFListTag.getCompound(i)));
            }
            attriGetherEffectInstances = EFList;

            CompoundTag SuitsNuma = nbt.getCompound("SuitsNum");
            for (String key : SuitsNuma.getAllKeys()) {
                String value = SuitsNuma.getString(key);
                SuitsNum.put(ExSuitHandle.LoadExSuit.get(key), Integer.parseInt(value));
            }

            CompoundTag syncContenta = nbt.getCompound("syncContent");
            for (String key : syncContenta.getAllKeys()) {
                String value = syncContenta.getString(key);
                syncContent.put(key,value);
            }

            CompoundTag itemsDamaget = nbt.getCompound("itemsDamage");
            for (String key : itemsDamaget.getAllKeys()) {
                String value = itemsDamaget.getString(key);
                itemsDamage.put(key, Float.parseFloat(value));
            }
        }
    }

    public static class PlayerVariablesSyncMessage {
        public PlayerVariables data;

        public PlayerVariablesSyncMessage(FriendlyByteBuf buffer) {
            this.data = new PlayerVariables();
            this.data.readNBT(buffer.readNbt());
        }

        public PlayerVariablesSyncMessage(PlayerVariables data) {
            this.data = data;
        }

        public static void buffer(PlayerVariablesSyncMessage message, FriendlyByteBuf buffer) {
            buffer.writeNbt((CompoundTag) message.data.writeNBT());
        }

        public static void handler(PlayerVariablesSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (!context.getDirection().getReceptionSide().isServer()) {
                    PlayerVariables variables = ((PlayerVariables) Minecraft.getInstance().player.getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
                    variables.Sitemstack = message.data.Sitemstack;
                    variables.craftIndex = message.data.craftIndex;
                    variables.Suits = message.data.Suits;
                    variables.SuitsNum = message.data.SuitsNum;
                    variables.attriGetherEffectInstances = message.data.attriGetherEffectInstances;
                    variables.itemsDamage = message.data.itemsDamage;
                    variables.syncContent = message.data.syncContent;

                }
            });
            context.setPacketHandled(true);
        }
    }
}
