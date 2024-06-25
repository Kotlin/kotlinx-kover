package org.jetbrains;

public final class TryWithResources {

    private TryWithResources() {}

    public static void test() throws Exception {
        try (AutoCloseable ignored = generate()) {
            System.out.println("action");
        }
    }

    private static AutoCloseable generate() {
        return () -> System.out.println("closed");
    }
}
