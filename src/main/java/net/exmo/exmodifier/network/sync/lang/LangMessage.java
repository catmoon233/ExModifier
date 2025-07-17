package net.exmo.exmodifier.network.sync.lang;
import com.google.common.collect.Maps;
import net.exmo.exmodifier.Config;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.client.LanguageLoader;
import net.exmo.exmodifier.content.element.ExElement;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public record LangMessage(LangMessageHandler langMessageHandler) {

    public static class LangMessageHandler {
        public   Map<String, Map<String, String>> LANGUAGES ;
        public static final ExSerialize<LangMessageHandler> EX_SERIALIZE  = ExSerialize.create(()-> new LangMessageHandler(new HashMap<>())).addStringMapField(
                "langs",
                langMessageHandler ->
                {
                    Map<String,String> map = Maps.newHashMap();
                    langMessageHandler.LANGUAGES.forEach((a,b)->{
                        map.put(a,LanguageLoader.mapToString(b));
                    });
                    return map;
                },
                (k,v)->{
                    Map<String,Map<String, String>> map = Maps.newHashMap();
                    v.forEach((a,b)->{
                        map.put(a,LanguageLoader.handle( b));
                    });
                    k.LANGUAGES = map;
                }

        );

        public LangMessageHandler(Map<String, Map<String, String>> LANGUAGES) {
            this.LANGUAGES = LANGUAGES;
        }
    }
    public static void encode(LangMessage msg, FriendlyByteBuf buffer) {
        buffer.writeNbt(LangMessageHandler.EX_SERIALIZE.toNbt(msg.langMessageHandler()));

    }

    public static LangMessage decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt(); // Adjust the length as needed
        if (tag != null) {
            return new LangMessage(LangMessageHandler.EX_SERIALIZE.fromNbt(tag));
        }
        return null;
    }

    public static void handle(LangMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LangMessageHandler langMessageHandler1 = msg.langMessageHandler();
            LanguageLoader.LANGUAGES.clear();
            LanguageLoader.LANGUAGES.putAll(langMessageHandler1.LANGUAGES);
        });
        ctx.get().setPacketHandled(true);
    }
}