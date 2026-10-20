package net.exmo.exmodifier.pack;

public enum SuggestSource {
    NONE,
    BOOL,
    OPERATION,
    UP_EVENT,
    TRIGGER,
    ATTRIBUTE,
    ITEM,
    ITEM_TAG,
    ENTITY,
    EFFECT,
    DAMAGE_TYPE,
    PARTICLE,
    ENTRY,
    SLOT,
    ELEMENT,
    QUALITY,
    ITEM_TYPE,
    SUIT,
    SPECIAL,
    SCHOOL,
    ATTRI_EFFECT,
    DYNAMIC_ATTRIBUTE;

    public PackKind kind() {
        return switch (this) {
            case ENTRY -> PackKind.MODIFIER_ENTRY;
            case SLOT -> PackKind.SLOT;
            case ELEMENT -> PackKind.ELEMENT;
            case QUALITY -> PackKind.QUALITY;
            case ITEM_TYPE -> PackKind.ITEM_TYPE;
            case SUIT -> PackKind.SUIT;
            case ATTRI_EFFECT -> PackKind.ATTRI_EFFECT;
            case DYNAMIC_ATTRIBUTE -> PackKind.DYNAMIC_ATTRIBUTE;
            default -> null;
        };
    }
}
