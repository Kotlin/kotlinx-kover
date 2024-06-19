package org.jetbrains;

public final class TryWithResources {

    private TryWithResources() {}

    public static void test() throws Exception {
        try (AutoCloseable ignored = () -> System.out.println("closed")) {
            System.out.println("action");
        }
    }
}
