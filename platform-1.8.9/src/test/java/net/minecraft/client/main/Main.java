package net.minecraft.client.main;

public final class Main {
    public static int invocations;

    private Main() {
    }

    public static void main(
            final String[] arguments) {
        invocations++;
    }
}
