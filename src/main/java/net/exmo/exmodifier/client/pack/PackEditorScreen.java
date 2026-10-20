package net.exmo.exmodifier.client.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.exmo.exmodifier.pack.PackField;
import net.exmo.exmodifier.pack.PackIds;
import net.exmo.exmodifier.pack.PackKind;
import net.exmo.exmodifier.pack.PackLiveIndex;
import net.exmo.exmodifier.pack.PackMutations;
import net.exmo.exmodifier.pack.PackNetwork;
import net.exmo.exmodifier.pack.PackSuggestions;
import net.exmo.exmodifier.pack.SuggestSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PackEditorScreen extends Screen {
    private static final int ROW = 14;
    private Sync sync = new Sync();
    private String pack = "";
    private PackKind kind = PackKind.ELEMENT;
    private String entryId = "";
    private JsonObject draft = new JsonObject();
    private boolean dirty;
    private String status = "";
    private int packScroll;
    private int kindScroll;
    private int entryScroll;
    private int formScroll;
    private int pickScroll;
    private int suggestIndex;
    private Mode mode = Mode.NONE;
    private Pick pick = Pick.NONE;
    private long confirmDeleteUntil;
    private final List<Hit> hits = new ArrayList<>();
    private List<PackSuggestions.Hit> popup = List.of();
    private EditBox packName;
    private EditBox namespaceBox;
    private EditBox search;
    private EditBox newId;
    private EditBox editor;
    private EditBox pickerSearch;
    private Button createButton;
    private Button applyButton;
    private Button saveButton;
    private Button closeButton;
    private Button deletePackButton;
    private Button deleteEntryButton;
    private String confirmToken = "";
    private PackField.FieldType editorType = PackField.FieldType.STRING;
    private int clipTop;
    private int clipBottom = Integer.MAX_VALUE;

    public PackEditorScreen(String json) {
        super(Component.translatable("pack.exmodifier.title"));
        this.sync = Sync.parse(json);
        selectFromSync(true);
    }

    public static void accept(String json) {
        Minecraft minecraft = Minecraft.getInstance();
        Sync incoming = Sync.parse(json);
        if (minecraft.screen instanceof PackEditorScreen screen) {
            screen.apply(incoming);
            return;
        }
        if (incoming.open) minecraft.setScreen(new PackEditorScreen(json));
        else if (minecraft.player != null && incoming.message != null && !incoming.message.isBlank()) {
            minecraft.player.displayClientMessage(Component.literal(incoming.message), false);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        packName = addRenderableWidget(new EditBox(font, 0, 0, 70, 14, Component.translatable("pack.exmodifier.create")));
        packName.setMaxLength(32);
        namespaceBox = addRenderableWidget(new EditBox(font, 0, 0, 70, 14, Component.translatable("pack.exmodifier.namespace")));
        namespaceBox.setMaxLength(32);
        search = addRenderableWidget(new EditBox(font, 0, 0, 80, 14, Component.translatable("pack.exmodifier.search")));
        search.setMaxLength(64);
        search.setHint(Component.translatable("pack.exmodifier.search"));
        newId = addRenderableWidget(new EditBox(font, 0, 0, 80, 14, Component.translatable("pack.exmodifier.new_entry")));
        newId.setMaxLength(96);
        editor = addRenderableWidget(new EditBox(font, 0, 0, 80, 14, Component.literal("")));
        editor.setMaxLength(1024);
        editor.setVisible(false);
        editor.setResponder(this::onEditorChanged);
        pickerSearch = addRenderableWidget(new EditBox(font, 0, 0, 120, 14, Component.translatable("pack.exmodifier.search")));
        pickerSearch.setMaxLength(64);
        pickerSearch.setVisible(false);
        pickerSearch.setResponder(value -> pickScroll = 0);
        createButton = addRenderableWidget(Button.builder(Component.translatable("pack.exmodifier.create"), button -> createPack()).bounds(0, 0, 36, 14).build());
        applyButton = addRenderableWidget(Button.builder(Component.translatable("pack.exmodifier.apply"), button -> send("apply", packBody())).bounds(0, 0, 40, 16).build());
        saveButton = addRenderableWidget(Button.builder(Component.translatable("pack.exmodifier.save"), button -> save()).bounds(0, 0, 36, 14).build());
        closeButton = addRenderableWidget(Button.builder(Component.translatable("pack.exmodifier.close"), button -> onClose()).bounds(0, 0, 36, 16).build());
        deletePackButton = addRenderableWidget(Button.builder(Component.translatable("pack.exmodifier.delete"), button -> deletePack()).bounds(0, 0, 36, 14).build());
        deleteEntryButton = addRenderableWidget(Button.builder(Component.translatable("pack.exmodifier.delete"), button -> deleteEntry()).bounds(0, 0, 36, 14).build());
        layout();
    }

    private void layout() {
        int gap = 4;
        packName.setPosition(8, height - 32);
        packName.setWidth(Math.max(48, packWidth() - 42));
        namespaceBox.setPosition(8, height - 16);
        namespaceBox.setWidth(Math.max(36, packWidth() - 40));
        createButton.setPosition(8 + packName.getWidth() + 2, height - 32);
        deletePackButton.setPosition(8 + namespaceBox.getWidth() + 2, height - 16);
        deletePackButton.active = !pack.isBlank();
        deletePackButton.setMessage(confirming("pack:" + pack)
                ? Component.translatable("pack.exmodifier.confirm")
                : Component.translatable("pack.exmodifier.delete"));
        search.setPosition(8 + packWidth() + kindWidth() + gap * 2, 24);
        search.setWidth(Math.max(60, entryWidth() - 4));
        newId.setPosition(search.getX(), height - 32);
        newId.setWidth(Math.max(40, entryWidth() - 36));
        applyButton.setPosition(width - 86, 4);
        closeButton.setPosition(width - 42, 4);
        saveButton.setPosition(formX(), 24);
        deleteEntryButton.setPosition(formX() + 40, 24);
        deleteEntryButton.visible = !entryId.isBlank();
        deleteEntryButton.active = !pack.isBlank() && !entryId.isBlank();
        deleteEntryButton.setMessage(confirming("entry:" + pack + "/" + kind.folder() + "/" + entryId)
                ? Component.translatable("pack.exmodifier.confirm")
                : Component.translatable("pack.exmodifier.delete"));
        pickerSearch.setPosition(width / 2 - 90, 36);
        pickerSearch.setWidth(180);
        pickerSearch.setVisible(mode != Mode.NONE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        layout();
        hits.clear();
        graphics.fill(0, 0, width, height, 0xF0101218);
        graphics.fill(0, 0, width, 22, 0xFF1B2836);
        graphics.drawString(font, title, 8, 7, 0xFF6EC8E0);
        panel(graphics, 6, 24, packWidth(), bodyHeight());
        panel(graphics, 8 + packWidth(), 24, kindWidth(), bodyHeight());
        panel(graphics, search.getX() - 2, 40, entryWidth(), bodyHeight() - 34);
        panel(graphics, formX(), 40, formWidth(), bodyHeight() - 16);
        graphics.drawString(font, Component.translatable("pack.exmodifier.col_pack"), 8, 26, 0xFF6EC8E0);
        graphics.drawString(font, Component.translatable("pack.exmodifier.col_kind"), 10 + packWidth(), 26, 0xFF6EC8E0);
        graphics.drawString(font, Component.translatable("pack.exmodifier.col_entry"), search.getX(), 42, 0xFF6EC8E0);
        renderColumn(graphics, 8, 38, packWidth() - 4, bodyHeight() - 52, packNames(), packScroll, pack, mouseX, mouseY, this::selectPack, false);
        renderColumn(graphics, 10 + packWidth(), 38, kindWidth() - 4, bodyHeight() - 16, kindLabels(), kindScroll, kind.folder(), mouseX, mouseY, this::selectKindLabel, true);
        renderColumn(graphics, search.getX(), 54, entryWidth() - 4, bodyHeight() - 90, filteredEntries(), entryScroll, entryId, mouseX, mouseY, this::selectEntry, false);
        renderForm(graphics, mouseX, mouseY);
        graphics.drawString(font, trim(status, Math.max(40, width - 260)), Math.min(168, width / 5), 7, dirty ? 0xFFE6C35C : 0xFFB7C2CE);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (mode != Mode.NONE) {
            hits.clear();
            renderPicker(graphics, mouseX, mouseY);
        } else if (editor.isVisible() && !popup.isEmpty()) {
            hits.clear();
            renderPopup(graphics, mouseX, mouseY);
        }
    }

    private void renderForm(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = formX() + 4;
        int y = 44 - formScroll;
        int w = formWidth() - 8;
        graphics.enableScissor(formX() + 1, 41, formX() + formWidth() - 1, 24 + bodyHeight() - 1);
        clipTop = 41;
        clipBottom = 24 + bodyHeight() - 1;
        if (pack.isBlank()) {
            graphics.drawString(font, Component.translatable("pack.exmodifier.pick_pack"), x, 48, 0xFF8B97A6);
        } else if (entryId.isBlank()) {
            graphics.drawString(font, Component.translatable("pack.exmodifier.pick_entry"), x, 48, 0xFF8B97A6);
            graphics.drawString(font, Component.translatable("pack.exmodifier.status_chain"), x, 62, 0xFF6EC8E0);
        } else {
            graphics.drawString(font, trim(entryId, w - 44), x, y, 0xFFE7EEF4);
            graphics.drawString(font, Component.translatable(kind.labelKey()), x, y + 12, 0xFF8B97A6);
            y += 28;
            for (PackField field : kind.fields()) {
                y = renderField(graphics, field, field.key(), x, y, w, mouseX, mouseY);
            }
            y = renderUnknown(graphics, x, y, w);
        }
        clipTop = 0;
        clipBottom = Integer.MAX_VALUE;
        hit(formX() + formWidth() - 28, 24, 24, 14, () -> formScroll = Math.max(0, formScroll - 40), "");
        graphics.disableScissor();
        graphics.fill(search.getX(), height - 50, search.getX() + entryWidth() - 4, height - 36, 0xFF1E2A36);
        graphics.drawString(font, Component.translatable("pack.exmodifier.import"), search.getX() + 2, height - 46, 0xFF8EC8E8);
        hit(search.getX(), height - 50, entryWidth() - 4, 14, this::openImport, "import");
        int newX = newId.getX() + newId.getWidth() + 2;
        graphics.fill(newX, height - 32, newX + 32, height - 18, 0xFF234858);
        graphics.drawString(font, Component.translatable("pack.exmodifier.new_entry"), newX + 2, height - 28, 0xFFE7EEF4);
        hit(newX, height - 32, 32, 14, this::createEntry, "new");
    }

    private int renderField(GuiGraphics graphics, PackField field, String path, int x, int y, int w, int mouseX, int mouseY) {
        Component label = Component.translatable(field.labelKey());
        graphics.drawString(font, label, x, y, 0xFF9AD0E8);
        graphics.drawString(font, field.key(), x + font.width(label) + 6, y, 0xFF66707C);
        y += 12;
        return switch (field.type()) {
            case BOOL -> boolRow(graphics, path, x, y, w);
            case INT, FLOAT, DOUBLE, STRING, COLOR, ID -> scalarRow(graphics, field, path, x, y, w);
            case ID_LIST, STRING_LIST -> listRow(graphics, field, path, x, y, w, mouseX, mouseY);
            case FLOAT_MAP, INT_MAP -> mapRow(graphics, field, path, x, y, w);
            case ATTRIBUTE_LIST -> attributeRows(graphics, path, x, y, w);
            case EFFECT_LIST -> effectRows(graphics, path, x, y, w);
            case SUIT_LEVEL_LIST -> suitRows(graphics, x, y, w, mouseX, mouseY);
        };
    }

    private int boolRow(GuiGraphics graphics, String path, int x, int y, int w) {
        boolean value = draft.has(path) && draft.get(path).isJsonPrimitive() && draft.get(path).getAsBoolean();
        graphics.fill(x, y, x + 36, y + 12, value ? 0xFF2E6B45 : 0xFF3A2A2E);
        graphics.drawString(font, value ? "true" : "false", x + 4, y + 2, 0xFFE7EEF4);
        hit(x, y, 36, 12, () -> {
            draft.addProperty(path, !value);
            dirty = true;
        }, path);
        return y + 16;
    }

    private int scalarRow(GuiGraphics graphics, PackField field, String path, int x, int y, int w) {
        String value = scalarText(path, field);
        graphics.fill(x, y, x + Math.min(w, 220), y + 12, 0xFF121820);
        graphics.drawString(font, trim(value.isEmpty() ? "…" : value, Math.min(w, 210)), x + 3, y + 2, value.isEmpty() ? 0xFF66707C : 0xFFE7EEF4);
        hit(x, y, Math.min(w, 220), 12, () -> openEditor(path, field, value), path);
        if (field.source() != SuggestSource.NONE) {
            graphics.drawString(font, "+", x + Math.min(w, 224), y + 2, 0xFF6EC8E0);
            hit(x + Math.min(w, 220), y, 14, 12, () -> openPick(path, field.source(), false), "pick");
        }
        return y + 16;
    }

    private int listRow(GuiGraphics graphics, PackField field, String path, int x, int y, int w, int mouseX, int mouseY) {
        List<String> values = PackMutations.strings(draft, path);
        int cursor = x;
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            int chip = Math.min(160, font.width(value) + 16);
            if (cursor + chip > x + w) {
                cursor = x;
                y += 14;
            }
            graphics.fill(cursor, y, cursor + chip, y + 12, 0xFF1E2A36);
            graphics.drawString(font, trim(value, chip - 14), cursor + 2, y + 2, originColor(value, field.source()));
            int removeAt = cursor + chip - 10;
            int index = i;
            hit(removeAt, y, 10, 12, () -> {
                PackMutations.remove(draft, path, Integer.toString(index));
                dirty = true;
            }, "remove");
            graphics.drawString(font, "x", removeAt, y + 2, 0xFFD35D6E);
            cursor += chip + 2;
        }
        graphics.drawString(font, "+", cursor + 2, y + 2, 0xFF6EC8E0);
        int addX = cursor;
        hit(addX, y, 14, 12, () -> openPick(path, field.source(), true), "add");
        return y + 16;
    }

    private int mapRow(GuiGraphics graphics, PackField field, String path, int x, int y, int w) {
        PackField.FieldType numberType = field.type() == PackField.FieldType.INT_MAP ? PackField.FieldType.INT : PackField.FieldType.FLOAT;
        if (draft.has(path) && draft.get(path).isJsonObject()) {
            for (var entry : draft.getAsJsonObject(path).entrySet()) {
                String key = entry.getKey();
                String raw = entry.getValue().isJsonPrimitive() ? entry.getValue().getAsString() : entry.getValue().toString();
                graphics.drawString(font, trim(key, w / 2 - 4), x, y + 2, originColor(key, field.source()));
                graphics.fill(x + w / 2, y, x + w - 14, y + 12, 0xFF121820);
                graphics.drawString(font, trim(raw, w / 2 - 16), x + w / 2 + 2, y + 2, 0xFFE7EEF4);
                hit(x + w / 2, y, Math.max(16, w / 2 - 14), 12, () -> openEditor(path + "." + key, new PackField(key, numberType, SuggestSource.NONE), raw), "map-value");
                hit(x + w - 12, y, 12, 12, () -> {
                    PackMutations.removeMap(draft, path, key);
                    dirty = true;
                }, "remove");
                graphics.drawString(font, "x", x + w - 10, y + 2, 0xFFD35D6E);
                y += 14;
            }
        }
        graphics.drawString(font, "+", x, y + 2, 0xFF6EC8E0);
        hit(x, y, 80, 12, () -> openPick(path, field.source(), true), "map");
        return y + 16;
    }

    private int attributeRows(GuiGraphics graphics, String path, int x, int y, int w) {
        JsonArray array = array(path);
        int opW = Math.min(72, Math.max(42, w / 5));
        int numW = 34;
        for (int i = 0; i < array.size(); i++) {
            if (!array.get(i).isJsonObject()) continue;
            JsonObject spec = array.get(i).getAsJsonObject();
            String attribute = text(spec, "attribute");
            String amount = text(spec, "amount");
            String perLevel = text(spec, "amount_per_level");
            String operation = text(spec, "operation");
            if (operation.isEmpty()) operation = "add_value";
            int removeX = x + w - 12;
            int opX = removeX - opW;
            int perX = opX - numW - 1;
            int amountX = perX - numW - 1;
            graphics.fill(x, y, x + w, y + 12, 0xFF121820);
            graphics.drawString(font, trim(attribute.isEmpty() ? "attribute" : attribute, Math.max(16, amountX - x - 4)), x + 2, y + 2, originColor(attribute, SuggestSource.ATTRIBUTE));
            graphics.drawString(font, trim(amount.isEmpty() ? "0" : amount, numW - 2), amountX, y + 2, 0xFFE7EEF4);
            graphics.drawString(font, trim(perLevel.isEmpty() ? "lv" : perLevel, numW - 2), perX, y + 2, 0xFF8B97A6);
            graphics.drawString(font, trim(operation, opW - 2), opX, y + 2, 0xFF9AD0E8);
            graphics.drawString(font, "x", removeX, y + 2, 0xFFD35D6E);
            int index = i;
            String amountValue = amount.isEmpty() ? "0" : amount;
            String perValue = perLevel.isEmpty() ? "0" : perLevel;
            String operationValue = operation;
            hit(x, y, Math.max(12, amountX - x), 12, () -> openPick(path + "." + index + ".attribute", SuggestSource.ATTRIBUTE, false), attribute);
            hit(amountX, y, numW, 12, () -> openEditor(path + "." + index + ".amount", new PackField("amount", PackField.FieldType.DOUBLE, SuggestSource.NONE), amountValue), "amount");
            hit(perX, y, numW, 12, () -> openEditor(path + "." + index + ".amount_per_level", new PackField("amount_per_level", PackField.FieldType.DOUBLE, SuggestSource.NONE), perValue), "per-level");
            hit(opX, y, opW, 12, () -> openPick(path + "." + index + ".operation", SuggestSource.OPERATION, false), operationValue);
            hit(removeX, y, 12, 12, () -> {
                PackMutations.remove(draft, path, Integer.toString(index));
                dirty = true;
            }, "remove");
            y += 14;
        }
        graphics.drawString(font, "+", x, y + 2, 0xFF6EC8E0);
        hit(x, y, 90, 12, () -> {
            PackMutations.addAttribute(draft, path, "minecraft:generic.attack_damage", 1.0D, 0.0D, "add_value", "");
            dirty = true;
        }, "add-attribute");
        return y + 16;
    }

    private int effectRows(GuiGraphics graphics, String path, int x, int y, int w) {
        JsonArray array = array(path);
        for (int i = 0; i < array.size(); i++) {
            if (!array.get(i).isJsonObject()) continue;
            JsonObject spec = array.get(i).getAsJsonObject();
            String effect = text(spec, "effect");
            String duration = text(spec, "duration");
            String amplifier = text(spec, "amplifier");
            boolean visible = spec.has("visible") && spec.get("visible").isJsonPrimitive() && spec.get("visible").getAsBoolean();
            int removeX = x + w - 12;
            int visX = removeX - 14;
            int ampX = visX - 28;
            int durX = ampX - 36;
            graphics.fill(x, y, x + w, y + 12, 0xFF121820);
            graphics.drawString(font, trim(effect.isEmpty() ? "effect" : effect, Math.max(16, durX - x - 4)), x + 2, y + 2, originColor(effect, SuggestSource.EFFECT));
            graphics.drawString(font, trim(duration.isEmpty() ? "40" : duration, 34), durX, y + 2, 0xFFE7EEF4);
            graphics.drawString(font, trim(amplifier.isEmpty() ? "0" : amplifier, 26), ampX, y + 2, 0xFF8B97A6);
            graphics.drawString(font, visible ? "v" : "-", visX, y + 2, visible ? 0xFF8FD18F : 0xFF66707C);
            graphics.drawString(font, "x", removeX, y + 2, 0xFFD35D6E);
            int index = i;
            String durationValue = duration.isEmpty() ? "40" : duration;
            String amplifierValue = amplifier.isEmpty() ? "0" : amplifier;
            hit(x, y, Math.max(12, durX - x), 12, () -> openPick(path + "." + index + ".effect", SuggestSource.EFFECT, false), effect);
            hit(durX, y, 36, 12, () -> openEditor(path + "." + index + ".duration", new PackField("duration", PackField.FieldType.INT, SuggestSource.NONE), durationValue), "duration");
            hit(ampX, y, 28, 12, () -> openEditor(path + "." + index + ".amplifier", new PackField("amplifier", PackField.FieldType.INT, SuggestSource.NONE), amplifierValue), "amplifier");
            hit(visX, y, 14, 12, () -> {
                PackMutations.set(draft, path + "." + index + ".visible", new com.google.gson.JsonPrimitive(!visible));
                dirty = true;
            }, "visible");
            hit(removeX, y, 12, 12, () -> {
                PackMutations.remove(draft, path, Integer.toString(index));
                dirty = true;
            }, "remove");
            y += 14;
        }
        graphics.drawString(font, "+", x, y + 2, 0xFF6EC8E0);
        hit(x, y, 80, 12, () -> {
            PackMutations.addEffect(draft, path, "minecraft:speed", 40, 0, false, false);
            dirty = true;
        }, "add-effect");
        return y + 16;
    }

    private int suitRows(GuiGraphics graphics, int x, int y, int w, int mouseX, int mouseY) {
        JsonArray levels = array("levels");
        for (int i = 0; i < levels.size(); i++) {
            graphics.drawString(font, Component.translatable("pack.exmodifier.level", i + 1), x, y, 0xFFE6C35C);
            int index = i;
            hit(x + w - 12, y, 12, 12, () -> {
                PackMutations.remove(draft, "levels", Integer.toString(index));
                dirty = true;
            }, "remove-level");
            graphics.drawString(font, "x", x + w - 10, y, 0xFFD35D6E);
            y += 12;
            y = scalarRow(graphics, PackKind.resolve(PackKind.SUIT, "levels.0.pieces"), "levels." + i + ".pieces", x, y, w);
            y = scalarRow(graphics, PackKind.resolve(PackKind.SUIT, "levels.0.trigger"), "levels." + i + ".trigger", x, y, w);
            graphics.drawString(font, Component.translatable("field.exmodifier.attributes"), x + 6, y, 0xFF8B97A6);
            y += 11;
            y = attributeRows(graphics, "levels." + i + ".attributes", x + 6, y, w - 6);
            graphics.drawString(font, Component.translatable("field.exmodifier.effects"), x + 6, y, 0xFF8B97A6);
            y += 11;
            y = effectRows(graphics, "levels." + i + ".effects", x + 6, y, w - 6);
            graphics.drawString(font, Component.translatable("field.exmodifier.special_effects"), x + 6, y, 0xFF8B97A6);
            y += 11;
            y = listRow(graphics, PackKind.resolve(PackKind.SUIT, "levels.0.special_effects"), "levels." + i + ".special_effects", x + 6, y, w - 6, mouseX, mouseY);
            graphics.drawString(font, Component.translatable("field.exmodifier.commands"), x + 6, y, 0xFF8B97A6);
            y += 11;
            y = listRow(graphics, PackKind.resolve(PackKind.SUIT, "levels.0.commands"), "levels." + i + ".commands", x + 6, y, w - 6, mouseX, mouseY);
        }
        graphics.drawString(font, "+", x, y + 2, 0xFF6EC8E0);
        hit(x, y, 100, 12, () -> {
            PackMutations.addSuitLevel(draft, Math.max(1, levels.size() + 1), "exmodifier:tick");
            dirty = true;
        }, "add-level");
        return y + 16;
    }

    private int renderUnknown(GuiGraphics graphics, int x, int y, int w) {
        for (String key : draft.keySet()) {
            if (kind.fields().stream().anyMatch(field -> field.key().equals(key))) continue;
            graphics.drawString(font, trim(key + " = " + draft.get(key), w), x, y, 0xFF66707C);
            y += 12;
        }
        return y;
    }

    private void renderPicker(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(0, 0, width, height, 0xC0101018);
        int x = width / 2 - 140;
        int y = 28;
        int w = 280;
        int h = height - 56;
        panel(graphics, x, y, w, h);
        graphics.drawString(font, Component.translatable(mode == Mode.IMPORT ? "pack.exmodifier.import" : "pack.exmodifier.pick"), x + 8, y + 6, 0xFF6EC8E0);
        graphics.drawString(font, Component.translatable("pack.exmodifier.status_chain"), x + 8, y + 32, 0xFF8B97A6);
        List<PackSuggestions.Hit> hitsNow = pickerHits();
        int rowY = y + 48 - pickScroll;
        graphics.enableScissor(x + 4, y + 46, x + w - 4, y + h - 8);
        for (int i = 0; i < hitsNow.size(); i++) {
            PackSuggestions.Hit hit = hitsNow.get(i);
            int ry = rowY + i * ROW;
            if (ry + ROW < y + 46 || ry > y + h) continue;
            graphics.fill(x + 6, ry, x + w - 6, ry + ROW - 1, 0xFF182028);
            graphics.drawString(font, trim(tag(hit.origin()) + " " + hit.id(), w - 20), x + 8, ry + 3, originColor(hit.origin()));
            int index = i;
            this.hit(x + 6, ry, w - 12, ROW - 1, () -> choose(hitsNow.get(index)), hit.id());
        }
        graphics.disableScissor();
    }

    private void renderPopup(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = editor.getX();
        int y = editor.getY() + 16;
        int w = Math.max(160, editor.getWidth());
        int shown = Math.min(8, popup.size());
        graphics.fill(x, y, x + w, y + shown * ROW + 2, 0xF0182028);
        for (int i = 0; i < shown; i++) {
            PackSuggestions.Hit hit = popup.get(i);
            int ry = y + 1 + i * ROW;
            if (i == suggestIndex) graphics.fill(x + 1, ry, x + w - 1, ry + ROW - 1, 0xFF234858);
            graphics.drawString(font, trim(tag(hit.origin()) + " " + hit.id(), w - 8), x + 3, ry + 3, originColor(hit.origin()));
            int index = i;
            this.hit(x, ry, w, ROW, () -> acceptSuggestion(popup.get(index).id()), hit.id());
        }
    }

    private void renderColumn(GuiGraphics graphics, int x, int y, int w, int h, List<String> items, int scroll, String selected, int mouseX, int mouseY, java.util.function.Consumer<String> click, boolean translateKinds) {
        graphics.enableScissor(x, y, x + w, y + h);
        for (int i = 0; i < items.size(); i++) {
            int ry = y + i * ROW - scroll;
            if (ry + ROW < y || ry > y + h) continue;
            String value = items.get(i);
            boolean on = value.equals(selected);
            graphics.fill(x, ry, x + w, ry + ROW - 1, on ? 0xFF234858 : 0x00182028);
            String shown = value;
            if (translateKinds) {
                PackKind parsed = PackKind.byFolder(value);
                if (parsed != null) shown = Component.translatable(parsed.labelKey()).getString();
            }
            graphics.drawString(font, trim(shown, w - 6), x + 2, ry + 3, on ? 0xFFE7EEF4 : 0xFFB7C2CE);
            hit(x, ry, w, ROW - 1, () -> click.accept(value), value);
        }
        graphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mode != Mode.NONE) {
            boolean outside = mouseX < width / 2.0 - 140 || mouseX > width / 2.0 + 140 || mouseY < 28 || mouseY > height - 28;
            if (outside && !pickerSearch.isMouseOver(mouseX, mouseY)) {
                closePick();
                return true;
            }
            for (Hit hit : hits) {
                if (hit.contains(mouseX, mouseY)) {
                    hit.action.run();
                    return true;
                }
            }
            if (pickerSearch.isMouseOver(mouseX, mouseY)) return super.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        for (Hit hit : hits) {
            if (hit.contains(mouseX, mouseY)) {
                hit.action.run();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int delta = (int) (-scrollY * ROW);
        if (mode != Mode.NONE) pickScroll = Math.max(0, pickScroll + delta);
        else if (mouseX < 8 + packWidth()) packScroll = Math.max(0, packScroll + delta);
        else if (mouseX < 8 + packWidth() + kindWidth()) kindScroll = Math.max(0, kindScroll + delta);
        else if (mouseX < formX()) entryScroll = Math.max(0, entryScroll + delta);
        else formScroll = Math.max(0, formScroll + delta);
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_S && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            save();
            return true;
        }
        if (editor.isFocused()) {
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_TAB) {
                if (!popup.isEmpty()) acceptSuggestion(popup.get(Math.min(suggestIndex, popup.size() - 1)).id());
                else commitEditor();
                return true;
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                closeEditor();
                return true;
            }
            if (key == GLFW.GLFW_KEY_DOWN) {
                suggestIndex = Math.min(popup.size() - 1, suggestIndex + 1);
                return true;
            }
            if (key == GLFW.GLFW_KEY_UP) {
                suggestIndex = Math.max(0, suggestIndex - 1);
                return true;
            }
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (mode != Mode.NONE) {
                closePick();
                return true;
            }
            onClose();
            return true;
        }
        return super.keyPressed(key, scan, modifiers);
    }

    @Override
    public void onClose() {
        PackNetwork.request("close", "{}");
        super.onClose();
    }

    private void apply(Sync incoming) {
        boolean keepDraft = dirty && incoming.selectId.isBlank();
        sync = incoming;
        status = incoming.message == null ? "" : incoming.message;
        if (!incoming.ok && !incoming.message.isBlank()) dirty = true;
        if (!incoming.selectPack.isBlank()) pack = incoming.selectPack;
        if (!incoming.selectKind.isBlank()) {
            PackKind parsed = PackKind.byFolder(incoming.selectKind);
            if (parsed != null) kind = parsed;
        }
        if (!incoming.selectId.isBlank()) {
            entryId = incoming.selectId;
            dirty = false;
        }
        if (!keepDraft) reloadDraft();
        if (pack.isBlank() && !sync.packs.isEmpty()) selectFromSync(false);
    }

    private void selectFromSync(boolean initial) {
        if (!sync.packs.isEmpty() && (pack.isBlank() || sync.pack(pack) == null)) pack = sync.packs.getFirst().name;
        reloadDraft();
    }

    private void selectPack(String name) {
        if (!flush()) return;
        pack = name;
        entryId = "";
        draft = new JsonObject();
        dirty = false;
        namespaceBox.setValue(namespace());
    }

    private void selectKindLabel(String label) {
        if (!flush()) return;
        PackKind parsed = PackKind.byFolder(label);
        if (parsed == null) return;
        kind = parsed;
        entryId = "";
        draft = new JsonObject();
    }

    private void selectEntry(String id) {
        if (!flush()) return;
        entryId = id;
        reloadDraft();
    }

    private void reloadDraft() {
        Doc doc = sync.doc(pack, kind, entryId);
        draft = doc == null ? new JsonObject() : PackMutations.object(parseElement(doc.json));
        dirty = false;
    }

    private void createPack() {
        String name = packName.getValue().trim();
        String namespace = namespaceBox.getValue().trim();
        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("namespace", namespace.isBlank() ? name : namespace);
        send("create", body.toString());
    }

    private void createEntry() {
        if (pack.isBlank() || kind == null) return;
        ResourceLocation id = PackIds.qualify(newId.getValue(), namespace());
        if (id == null) {
            status = "ID 不合法";
            return;
        }
        entryId = id.toString();
        draft = new JsonObject();
        dirty = true;
        save();
    }

    private boolean confirming(String token) {
        return token != null && token.equals(confirmToken) && System.currentTimeMillis() < confirmDeleteUntil;
    }

    private boolean confirm(String token, String message) {
        if (!confirming(token)) {
            confirmToken = token;
            confirmDeleteUntil = System.currentTimeMillis() + 2500L;
            status = message;
            return false;
        }
        confirmToken = "";
        confirmDeleteUntil = 0L;
        return true;
    }

    private void deletePack() {
        if (pack.isBlank()) return;
        if (!confirm("pack:" + pack, "再点一次删除包 " + pack)) return;
        dirty = false;
        JsonObject body = new JsonObject();
        body.addProperty("pack", pack);
        pack = "";
        entryId = "";
        draft = new JsonObject();
        send("delete", body.toString());
    }

    private void deleteEntry() {
        if (pack.isBlank() || entryId.isBlank() || kind == null) return;
        if (!confirm("entry:" + pack + "/" + kind.folder() + "/" + entryId, "再点一次删除 " + entryId)) return;
        dirty = false;
        JsonObject body = packBodyObject();
        body.addProperty("kind", kind.folder());
        body.addProperty("id", entryId);
        entryId = "";
        draft = new JsonObject();
        send("remove", body.toString());
    }

    private void openImport() {
        if (pack.isBlank() || kind == null) return;
        closeEditor();
        mode = Mode.IMPORT;
        pick = Pick.NONE;
        pickerSearch.setVisible(true);
        pickerSearch.setFocused(true);
        pickerSearch.setValue("");
        pickScroll = 0;
    }

    private void openPick(String path, SuggestSource source, boolean listAdd) {
        if (source == null) source = SuggestSource.NONE;
        if (listAdd && source == SuggestSource.NONE) {
            openEditor(path, new PackField(path, PackField.FieldType.STRING, SuggestSource.NONE), "");
            pick = new Pick(path, SuggestSource.NONE, true);
            return;
        }
        closeEditor();
        mode = Mode.PICK;
        pick = new Pick(path, source, listAdd);
        pickerSearch.setVisible(true);
        pickerSearch.setFocused(true);
        pickerSearch.setValue("");
        pickScroll = 0;
    }

    private void closePick() {
        mode = Mode.NONE;
        pickerSearch.setVisible(false);
        pickerSearch.setFocused(false);
    }

    private void choose(PackSuggestions.Hit hit) {
        if (mode == Mode.IMPORT) {
            JsonObject body = packBodyObject();
            body.addProperty("kind", kind.folder());
            body.addProperty("id", hit.id());
            send("import", body.toString());
            closePick();
            return;
        }
        if (pick.listAdd && (pathField(pick.path) == null || pathField(pick.path).isList())) {
            PackMutations.add(draft, pick.path, new com.google.gson.JsonPrimitive(hit.id()));
        } else if (pick.listAdd) {
            PackMutations.putMap(draft, pick.path, hit.id(), new com.google.gson.JsonPrimitive(1));
        } else {
            PackMutations.set(draft, pick.path, new com.google.gson.JsonPrimitive(hit.id()));
        }
        dirty = true;
        closePick();
    }

    private void openEditor(String path, PackField field, String value) {
        editorType = field == null ? PackField.FieldType.STRING : field.type();
        editor.setVisible(true);
        editor.setPosition(formX() + 4, 44);
        editor.setWidth(Math.min(220, formWidth() - 16));
        editor.setValue(value == null ? "" : value);
        editor.setFocused(true);
        pick = new Pick(path, field == null ? SuggestSource.NONE : field.source(), false);
        refreshPopup();
    }

    private void onEditorChanged(String value) {
        suggestIndex = 0;
        refreshPopup();
    }

    private void refreshPopup() {
        if (!editor.isVisible() || pick.source == SuggestSource.NONE) {
            popup = List.of();
            return;
        }
        popup = suggestions(pick.source, editor.getValue(), 8);
    }

    private void acceptSuggestion(String id) {
        editor.setValue(id);
        commitEditor();
    }

    private void commitEditor() {
        if (pick.path.isBlank()) {
            closeEditor();
            return;
        }
        if (pick.listAdd) {
            String value = editor.getValue().trim();
            if (value.isEmpty()) {
                status = "值不能为空";
                return;
            }
            PackMutations.add(draft, pick.path, new com.google.gson.JsonPrimitive(value));
            dirty = true;
            closeEditor();
            return;
        }
        PackField field = pathField(pick.path);
        if (field == null || field.isList() || field.isMap() || field.type() == PackField.FieldType.SUIT_LEVEL_LIST) {
            field = new PackField(pick.path, editorType, pick.source);
        }
        if (!PackMutations.setScalar(draft, field, pick.path, editor.getValue())) {
            status = "值无法解析";
            return;
        }
        dirty = true;
        closeEditor();
    }

    private void closeEditor() {
        editor.setVisible(false);
        editor.setFocused(false);
        popup = List.of();
    }

    private boolean save() {
        if (pack.isBlank() || entryId.isBlank()) return false;
        Optional<String> error = kind.invalid(draft);
        if (error.isPresent()) {
            status = error.get();
            return false;
        }
        JsonObject body = packBodyObject();
        body.addProperty("kind", kind.folder());
        body.addProperty("id", entryId);
        body.addProperty("json", draft.toString());
        dirty = false;
        send("upsert", body.toString());
        status = Component.translatable("pack.exmodifier.saving").getString();
        return true;
    }

    private boolean flush() {
        if (!dirty) return true;
        return save();
    }

    private void send(String action, String body) {
        PackNetwork.request(action, body);
    }

    private String packBody() {
        return packBodyObject().toString();
    }

    private JsonObject packBodyObject() {
        JsonObject body = new JsonObject();
        body.addProperty("pack", pack);
        return body;
    }

    private List<PackSuggestions.Hit> pickerHits() {
        if (mode == Mode.IMPORT && kind != null) {
            return PackSuggestions.merge(
                    sync.registered.getOrDefault(kind.folder(), List.of()),
                    sync.ids(pack, kind),
                    sync.idsExcept(pack, kind),
                    pickerSearch.getValue(),
                    200);
        }
        return suggestions(pick.source, pickerSearch.getValue(), 200);
    }

    private List<PackSuggestions.Hit> suggestions(SuggestSource source, String typed, int limit) {
        List<String> registered = new ArrayList<>(sync.registered.getOrDefault(source.kind() == null ? "" : source.kind().folder(), List.of()));
        registered.addAll(local(source));
        registered.addAll(sync.extra.getOrDefault(extraKey(source), List.of()));
        registered.addAll(PackLiveIndex.mentioned(draft, source));
        for (Pack view : sync.packs) {
            for (Doc doc : view.documents) {
                if (view.name.equals(pack) && kind != null && kind.folder().equals(doc.kind) && doc.id.equals(entryId)) continue;
                registered.addAll(PackLiveIndex.mentioned(parseElement(doc.json), source));
            }
        }
        List<String> inPack = source.kind() == null ? List.of() : sync.ids(pack, source.kind());
        if (source.kind() == kind && !entryId.isBlank() && !inPack.contains(entryId)) {
            inPack = new ArrayList<>(inPack);
            inPack.add(entryId);
        }
        List<String> other = source.kind() == null ? List.of() : sync.idsExcept(pack, source.kind());
        return PackSuggestions.merge(registered, inPack, other, typed, limit);
    }

    private List<String> local(SuggestSource source) {
        return switch (source) {
            case ATTRIBUTE -> names(BuiltInRegistries.ATTRIBUTE.keySet());
            case ITEM -> names(BuiltInRegistries.ITEM.keySet());
            case ENTITY -> names(BuiltInRegistries.ENTITY_TYPE.keySet());
            case EFFECT -> names(BuiltInRegistries.MOB_EFFECT.keySet());
            case PARTICLE -> names(BuiltInRegistries.PARTICLE_TYPE.keySet());
            case ITEM_TAG -> BuiltInRegistries.ITEM.getTagNames().map(TagKey::location).map(ResourceLocation::toString).sorted().toList();
            case BOOL -> PackSuggestions.booleans();
            case OPERATION -> PackSuggestions.operations();
            case UP_EVENT -> PackSuggestions.upEvents();
            case TRIGGER -> PackSuggestions.triggers();
            case SPECIAL -> net.exmo.exmodifier.content.special.SpecialEffects.knownIds().stream().map(ResourceLocation::toString).toList();
            case DAMAGE_TYPE -> damageTypes();
            default -> List.of();
        };
    }

    private List<String> damageTypes() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return List.of();
        return minecraft.level.registryAccess().registry(Registries.DAMAGE_TYPE)
                .map(registry -> names(registry.keySet()))
                .orElse(List.of());
    }

    private static List<String> names(Iterable<ResourceLocation> ids) {
        List<String> values = new ArrayList<>();
        for (ResourceLocation id : ids) values.add(id.toString());
        values.sort(String::compareTo);
        return values;
    }

    private static String extraKey(SuggestSource source) {
        return switch (source) {
            case SCHOOL -> "school";
            case SPECIAL -> "special";
            case DAMAGE_TYPE -> "damage_type";
            default -> "";
        };
    }

    private PackField pathField(String path) {
        return PackKind.resolve(kind, path);
    }

    private String scalarText(String path, PackField field) {
        JsonElement element = elementAt(path);
        if (element == null || element.isJsonNull()) return "";
        if (field.type() == PackField.FieldType.COLOR && element.isJsonPrimitive()) {
            try {
                return PackIds.colorText(element.getAsInt());
            } catch (RuntimeException ex) {
                return element.getAsString();
            }
        }
        return element.isJsonPrimitive() ? element.getAsString() : element.toString();
    }

    private JsonElement elementAt(String path) {
        String[] parts = path.split("\\.");
        JsonElement current = draft;
        for (String part : parts) {
            if (current == null) return null;
            if (part.matches("\\d+") && current.isJsonArray()) {
                int index = Integer.parseInt(part);
                current = index >= 0 && index < current.getAsJsonArray().size() ? current.getAsJsonArray().get(index) : null;
            } else if (current.isJsonObject() && current.getAsJsonObject().has(part)) {
                current = current.getAsJsonObject().get(part);
            } else return null;
        }
        return current;
    }

    private JsonArray array(String path) {
        JsonElement element = elementAt(path);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : new JsonArray();
    }

    private int originColor(String id, SuggestSource source) {
        if (id == null || id.isBlank()) return 0xFF8B97A6;
        for (PackSuggestions.Hit hit : suggestions(source, id, 20)) {
            if (hit.id().equals(id)) return originColor(hit.origin());
        }
        return 0xFFE7EEF4;
    }

    private static int originColor(PackSuggestions.Origin origin) {
        return switch (origin) {
            case PACK -> 0xFFE6C35C;
            case REGISTERED -> 0xFF8EC8E8;
            case BOTH -> 0xFF8FD18F;
            case OTHER -> 0xFFC8A2E8;
        };
    }

    private static String tag(PackSuggestions.Origin origin) {
        return switch (origin) {
            case PACK -> "[包内]";
            case REGISTERED -> "[已注册]";
            case BOTH -> "[两者]";
            case OTHER -> "[其他包]";
        };
    }

    private List<String> packNames() {
        return sync.packs.stream().map(pack -> pack.name).toList();
    }

    private List<String> kindLabels() {
        List<String> labels = new ArrayList<>();
        for (PackKind value : PackKind.values()) labels.add(value.folder());
        return labels;
    }

    private List<String> filteredEntries() {
        String needle = search.getValue().trim().toLowerCase();
        return sync.ids(pack, kind).stream().filter(id -> needle.isEmpty() || id.toLowerCase().contains(needle)).toList();
    }

    private String namespace() {
        Pack found = sync.pack(pack);
        return found == null || found.namespace == null || found.namespace.isBlank() ? pack : found.namespace;
    }

    private void hit(int x, int y, int w, int h, Runnable action, String tip) {
        if (y + h <= clipTop || y >= clipBottom || y + h < 0 || y > height) return;
        hits.add(new Hit(x, y, w, h, action));
    }

    private void panel(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0xF0161A22);
        graphics.fill(x, y, x + w, y + 1, 0xFF2C3440);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFF2C3440);
        graphics.fill(x, y, x + 1, y + h, 0xFF2C3440);
        graphics.fill(x + w - 1, y, x + w, y + h, 0xFF2C3440);
    }

    private String trim(String text, int maxWidth) {
        if (text == null) return "";
        if (font.width(text) <= maxWidth) return text;
        String end = "...";
        while (text.length() > 1 && font.width(text + end) > maxWidth) text = text.substring(0, text.length() - 1);
        return text + end;
    }

    private int packWidth() { return Math.min(120, Math.max(88, width / 7)); }
    private int kindWidth() { return Math.min(108, Math.max(84, width / 8)); }
    private int entryWidth() { return Math.min(156, Math.max(112, width / 6)); }
    private int formX() { return 8 + packWidth() + kindWidth() + entryWidth() + 12; }
    private int formWidth() { return Math.max(120, width - formX() - 6); }
    private int bodyHeight() { return height - 40; }

    private static String text(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : "";
    }

    private static JsonElement parseElement(String raw) {
        try {
            return JsonParser.parseString(raw == null || raw.isBlank() ? "{}" : raw);
        } catch (RuntimeException ex) {
            return new JsonObject();
        }
    }

    private enum Mode { NONE, IMPORT, PICK }

    private record Pick(String path, SuggestSource source, boolean listAdd) {
        private static final Pick NONE = new Pick("", SuggestSource.NONE, false);
    }

    private record Hit(int x, int y, int w, int h, Runnable action) {
        private boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private static final class Sync {
        private boolean open;
        private boolean ok = true;
        private String message = "";
        private String selectPack = "";
        private String selectKind = "";
        private String selectId = "";
        private final List<Pack> packs = new ArrayList<>();
        private final Map<String, List<String>> registered = new LinkedHashMap<>();
        private final Map<String, List<String>> extra = new LinkedHashMap<>();

        private static Sync parse(String raw) {
            Sync sync = new Sync();
            try {
                JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
                sync.open = root.has("open") && root.get("open").getAsBoolean();
                sync.ok = !root.has("ok") || root.get("ok").getAsBoolean();
                sync.message = text(root, "message");
                sync.selectPack = text(root, "selectPack");
                sync.selectKind = text(root, "selectKind");
                sync.selectId = text(root, "selectId");
                if (root.has("packs")) {
                    for (JsonElement element : root.getAsJsonArray("packs")) {
                        JsonObject pack = element.getAsJsonObject();
                        Pack view = new Pack();
                        view.name = text(pack, "name");
                        view.namespace = text(pack, "namespace");
                        if (pack.has("documents")) {
                            for (JsonElement docElement : pack.getAsJsonArray("documents")) {
                                JsonObject doc = docElement.getAsJsonObject();
                                view.documents.add(new Doc(text(doc, "kind"), text(doc, "id"), text(doc, "json")));
                            }
                        }
                        sync.packs.add(view);
                    }
                }
                readMap(root, "registered", sync.registered);
                readMap(root, "extra", sync.extra);
            } catch (RuntimeException ignored) {
                sync.ok = false;
                sync.message = "同步数据无法解析";
            }
            return sync;
        }

        private static void readMap(JsonObject root, String key, Map<String, List<String>> into) {
            if (!root.has(key) || !root.get(key).isJsonObject()) return;
            for (var entry : root.getAsJsonObject(key).entrySet()) {
                List<String> ids = new ArrayList<>();
                if (entry.getValue().isJsonArray()) {
                    entry.getValue().getAsJsonArray().forEach(element -> {
                        if (element.isJsonPrimitive()) ids.add(element.getAsString());
                    });
                }
                into.put(entry.getKey(), ids);
            }
        }

        private Pack pack(String name) {
            for (Pack pack : packs) if (pack.name.equals(name)) return pack;
            return null;
        }

        private Doc doc(String packName, PackKind kind, String id) {
            Pack found = pack(packName);
            if (found == null || kind == null) return null;
            for (Doc doc : found.documents) {
                if (kind.folder().equals(doc.kind) && doc.id.equals(id)) return doc;
            }
            return null;
        }

        private List<String> ids(String packName, PackKind kind) {
            Pack found = pack(packName);
            if (found == null || kind == null) return List.of();
            return found.documents.stream().filter(doc -> kind.folder().equals(doc.kind)).map(doc -> doc.id).sorted(Comparator.naturalOrder()).toList();
        }

        private List<String> idsExcept(String packName, PackKind kind) {
            List<String> ids = new ArrayList<>();
            for (Pack pack : packs) {
                if (pack.name.equals(packName)) continue;
                ids.addAll(ids(pack.name, kind));
            }
            return ids;
        }
    }

    private static final class Pack {
        private String name = "";
        private String namespace = "";
        private final List<Doc> documents = new ArrayList<>();
    }

    private record Doc(String kind, String id, String json) {}
}
