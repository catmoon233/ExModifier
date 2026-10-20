package net.exmo.exmodifier.pack;

public record PackField(String key, FieldType type, SuggestSource source) {
    public String labelKey() {
        return "field.exmodifier." + key;
    }

    public boolean isList() {
        return type == FieldType.ID_LIST || type == FieldType.STRING_LIST
                || type == FieldType.ATTRIBUTE_LIST || type == FieldType.EFFECT_LIST;
    }

    public boolean isMap() {
        return type == FieldType.FLOAT_MAP || type == FieldType.INT_MAP;
    }

    public enum FieldType {
        INT, FLOAT, DOUBLE, BOOL, STRING, COLOR,
        ID, ID_LIST, STRING_LIST,
        FLOAT_MAP, INT_MAP,
        ATTRIBUTE_LIST, EFFECT_LIST, SUIT_LEVEL_LIST
    }
}
