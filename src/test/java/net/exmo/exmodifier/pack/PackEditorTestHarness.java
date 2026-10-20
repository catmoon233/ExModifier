package net.exmo.exmodifier.pack;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.exmo.exmodifier.content.command.TokenArgument;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class PackEditorTestHarness {
    public static void main(String[] args) throws Exception {
        tokenKeepsColon();
        suggestionsIncludePackAndRegistered();
        mutationsAndCodec();
        storeRoundTrip();
        System.out.println("pack editor accepts resource ids and unregistered pack entries");
    }

    private static void tokenKeepsColon() throws CommandSyntaxException {
        CommandDispatcher<Object> word = new CommandDispatcher<>();
        word.register(LiteralArgumentBuilder.<Object>literal("pack")
                .then(RequiredArgumentBuilder.<Object, String>argument("id", StringArgumentType.word())
                        .executes(ctx -> 1)));
        boolean wordFailed = false;
        try {
            word.execute("pack exmodifier:sharp", new Object());
        } catch (CommandSyntaxException ex) {
            wordFailed = true;
        }
        check(wordFailed, "word() rejects colon ids");

        CommandDispatcher<Object> token = new CommandDispatcher<>();
        token.register(LiteralArgumentBuilder.<Object>literal("pack")
                .then(RequiredArgumentBuilder.<Object, String>argument("id", TokenArgument.token())
                        .executes(ctx -> {
                            check("exmodifier:sharp/a".equals(ctx.getArgument("id", String.class)), "token keeps colon and slash");
                            return 1;
                        })));
        token.execute("pack exmodifier:sharp/a", new Object());
    }

    private static void suggestionsIncludePackAndRegistered() {
        List<PackSuggestions.Hit> hits = PackSuggestions.merge(
                List.of("exmodifier:sharp", "exmodifier:guard"),
                List.of("mypack:new_edge", "exmodifier:sharp"),
                List.of("other:draft"),
                "sharp",
                20);
        check(hits.stream().anyMatch(hit -> hit.id().equals("exmodifier:sharp") && hit.origin() == PackSuggestions.Origin.BOTH),
                "registered id also in the pack is both");
        check(hits.stream().noneMatch(hit -> hit.id().equals("mypack:new_edge")), "filter keeps the typed suffix");
        List<PackSuggestions.Hit> all = PackSuggestions.merge(
                List.of("exmodifier:guard"),
                List.of("mypack:new_edge"),
                List.of(),
                "",
                20);
        check(all.stream().anyMatch(hit -> hit.id().equals("mypack:new_edge") && hit.origin() == PackSuggestions.Origin.PACK),
                "unregistered pack entry is suggested");
        check(all.stream().anyMatch(hit -> hit.id().equals("exmodifier:guard") && hit.origin() == PackSuggestions.Origin.REGISTERED),
                "registered entry is suggested");
    }

    private static void mutationsAndCodec() {
        for (PackKind kind : PackKind.values()) {
            check(kind.invalid(kind.emptyJson()).isEmpty(), "empty " + kind.folder() + " validates");
        }
        JsonObject suit = new JsonObject();
        PackMutations.add(suit, "entries", new JsonPrimitive("mypack:new_edge"));
        PackMutations.addAttribute(suit, "levels.0.attributes", "minecraft:generic.attack_damage", 1.0D, 0.5D, "add_value", "");
        check(suit.getAsJsonArray("entries").get(0).getAsString().equals("mypack:new_edge"), "suit references pack-local entry");
        check(PackKind.SUIT.invalid(suit).isEmpty(), "suit with pack-local entry validates");
        check(PackKind.resolve(PackKind.SUIT, "levels.0.attributes").type() == PackField.FieldType.ATTRIBUTE_LIST, "attribute path resolves");
    }

    private static void storeRoundTrip() throws Exception {
        Path root = Files.createTempDirectory("exmod-pack");
        PackStore store = new PackStore(root);
        check(store.create("blades", "blades").ok(), "create pack");
        ResourceLocation entry = ResourceLocation.parse("blades:new_edge");
        JsonObject entryJson = new JsonObject();
        PackMutations.add(entryJson, "item_types", new JsonPrimitive("exmodifier:weapon"));
        check(store.upsert("blades", PackKind.MODIFIER_ENTRY, entry, entryJson).ok(), "write pack entry");
        JsonObject suit = new JsonObject();
        PackMutations.add(suit, "entries", new JsonPrimitive("blades:new_edge"));
        check(store.upsert("blades", PackKind.SUIT, ResourceLocation.parse("blades:set"), suit).ok(), "write suit");
        check(store.ids("blades", PackKind.MODIFIER_ENTRY).contains("blades:new_edge"), "pack ids survive disk");
        check(store.ids("blades", PackKind.SUIT).contains("blades:set"), "suit id survives disk");
        check(Files.isRegularFile(root.resolve("exmod_blades").resolve("data").resolve("blades").resolve("exmodifier").resolve("modifier_entry").resolve("new_edge.json")),
                "datapack path is written");
        check(store.find(PackKind.MODIFIER_ENTRY, entry).isPresent(), "find pack document");
        JsonObject element = new JsonObject();
        element.addProperty("school", "mypack:blade");
        check(PackLiveIndex.mentioned(element, SuggestSource.SCHOOL).contains("mypack:blade"), "pack school is suggested");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
