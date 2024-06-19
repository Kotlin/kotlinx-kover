package org.jetbrains;

public final class TryWithResources {

    private TryWithResources() {}

    public static void test() throws Exception {
        final AutoCloseable autoCloseable = new AutoCloseable() {
            @Override
            public void close() {
                System.out.println("closed");
            }
        };

        try (autoCloseable) {
            System.out.println("action");
        }
    }
}
