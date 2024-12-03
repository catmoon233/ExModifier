package net.exmo.exmodifier.content.AttributeEffect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class AttriGetherEffectInstance {
    public int duration;
    public AttriGetherEffect attriGetherEffect;
    public int startDuration;
    public int amplifier;
    public boolean randomAttrUUID =false;
   // public ServerBossEvent bossEvent;
    public boolean showParticle;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getStartDuration() {
        return startDuration;
    }

    public void setStartDuration(int startDuration) {
        this.startDuration = startDuration;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public AttriGetherEffect getAttriGetherEffect() {
        return attriGetherEffect;
    }

    public void setAttriGetherEffect(AttriGetherEffect attriGetherEffect) {
        this.attriGetherEffect = attriGetherEffect;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }

    public boolean isShowParticle() {
        return showParticle;
    }

    public void setShowParticle(boolean showParticle) {
        this.showParticle = showParticle;
    }

    public boolean isShowBossBar() {
        return showBossBar;
    }

    public void setShowBossBar(boolean showBossBar) {
        this.showBossBar = showBossBar;
    }

    public UUID uuid;
    public boolean showBossBar;
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Duration", duration);
        nbt.putString("AttriGetherEffect", attriGetherEffect.getId().toString());
        nbt.putInt("Amplifier", amplifier);
        nbt.putBoolean("randomAttrUUID", randomAttrUUID);
        nbt.putBoolean("ShowParticle", showParticle);
        nbt.putUUID("UUID", uuid);
        nbt.putBoolean("ShowBossBar", showBossBar);
        nbt.putInt("StartDuration", startDuration);

//        if (bossEvent != null) {
//            CompoundTag bossEventNBT = getBossBarCompoundTag();
//            //Player
//
//            nbt.put("BossEvent", bossEventNBT);
//        }

        return nbt;
    }

//    private @NotNull CompoundTag getBossBarCompoundTag() {
//        CompoundTag bossEventNBT = new CompoundTag();
//        bossEventNBT.putString("Name", bossEvent.getName().getString());
//        bossEventNBT.putString("Color", bossEvent.getColor().getName());
//        ServerPlayer serverPlayer = bossEvent.getPlayers().stream().findFirst().orElse(null);
//        if (serverPlayer != null) bossEventNBT.putString("Player", serverPlayer.getName().getString());
//        bossEventNBT.putString("BossBarOverlay", bossEvent.getOverlay().getName());
//        bossEventNBT.putString("Player", bossEvent.getOverlay().getName());
//        bossEventNBT.putBoolean("IsVisible", bossEvent.isVisible());
//        bossEventNBT.putFloat("Progress", bossEvent.getProgress());
//        return bossEventNBT;
//    }

    public static AttriGetherEffectInstance fromNBT(CompoundTag nbt) {
        int duration = nbt.getInt("Duration");
        int startDuration = nbt.getInt("StartDuration");
        AttriGetherEffect attriGetherEffect = AttriGetherEffectHandle.getById(ResourceLocation.tryParse(nbt.getString("AttriGetherEffect"))); // 假设有一个注册表来获取 AttriGetherEffect
        int amplifier = nbt.getInt("Amplifier");
        boolean randomAttrUUID = nbt.getBoolean("randomAttrUUID");
        UUID uuid = nbt.getUUID("UUID");
        boolean showParticle = nbt.getBoolean("ShowParticle");
        boolean showBossBar = nbt.getBoolean("ShowBossBar");

 //       ServerBossEvent bossEvent = null;
//        if (nbt.contains("BossEvent", Tag.TAG_COMPOUND)) {
//            CompoundTag bossEventNBT = nbt.getCompound("BossEvent");
//            Component name = Component.translatable(bossEventNBT.getString("Name"));
//            int progress = bossEventNBT.getInt("Progress");
//            BossEvent.BossBarOverlay overlay = BossEvent.BossBarOverlay.valueOf(bossEventNBT.getString("BossBarOverlay"));
//
//            ServerPlayer serverPlayer = ServerPlayer..getPlayerByName(bossEventNBT.getString("Player"));
//            BossEvent.BossBarColor color = BossEvent.BossBarColor.valueOf(bossEventNBT.getString("Color"));
//            bossEvent = new ServerBossEvent(name, color, overlay);
//            bossEvent.setProgress(progress);
//            bossEvent.setVisible(bossEventNBT.getBoolean("IsVisible"));
//            bossEvent.addPlayer(serverPlayer);
//
//
//        }


        AttriGetherEffectInstance attriGetherEffectInstance = new AttriGetherEffectInstance(duration, attriGetherEffect, amplifier, showParticle, showBossBar,false);
        attriGetherEffectInstance.setUuid(uuid);
        attriGetherEffectInstance.setStartDuration(startDuration);
        attriGetherEffectInstance.randomAttrUUID = randomAttrUUID;
        return attriGetherEffectInstance;
    }
    public AttriGetherEffectInstance(int duration, AttriGetherEffect attriGetherEffect, int amplifier, boolean showParticle) {
        this.duration = duration;
        this.startDuration = duration;
        this.attriGetherEffect = attriGetherEffect;
        this.amplifier = amplifier;
        this.showParticle = showParticle;
        this.showBossBar = attriGetherEffect.isVisible();
        this.randomAttrUUID = attriGetherEffect.isRandomAttrUUID();
      //  this.bossEvent = bossEvent;
        this.uuid = UUID.randomUUID();

    }
    public AttriGetherEffectInstance(int duration, AttriGetherEffect attriGetherEffect, int amplifier, boolean showParticle, boolean showBossBar,boolean genUUID) {
        this.duration = duration;
        this.startDuration = duration;
        this.showParticle = showParticle;
        this.amplifier = amplifier;
        this.attriGetherEffect = attriGetherEffect;
        this.randomAttrUUID = attriGetherEffect.isRandomAttrUUID();
        this.showBossBar = showBossBar;
        if (genUUID) this.uuid = UUID.randomUUID();
    }
    public String getDescriptionId() {
        return this.attriGetherEffect.getLocalDescription();
    }
    public String toString() {
        String s;
        if (this.amplifier > 0) {
            s = this.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.describeDuration();
        } else {
            s = this.getDescriptionId() + ", Duration: " + this.describeDuration();
        }

        if (!this.showBossBar) {
            s = s + ", Particles: false";
        }

        if (!this.showParticle) {
            s = s + ", Show Particle: false";
        }

        return s;
    }

    private boolean hasRemainingDuration() {
        return this.isInfiniteDuration() || this.duration > 0;
    }

    private String describeDuration() {
        return this.isInfiniteDuration() ? "infinite" : Integer.toString(this.duration);
    }
    public boolean isInfiniteDuration() {
        return this.duration == -1;
    }

}