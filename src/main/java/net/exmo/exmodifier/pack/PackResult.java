package net.exmo.exmodifier.pack;

public record PackResult(boolean ok, String message) {
    public static PackResult ok(String message) {
        return new PackResult(true, message);
    }

    public static PackResult fail(String message) {
        return new PackResult(false, message);
    }
}
