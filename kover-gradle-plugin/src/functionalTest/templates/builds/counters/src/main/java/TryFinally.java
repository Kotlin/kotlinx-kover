package org.jetbrains;

public final class TryFinally {

    private TryFinally() {}

    public static void testWithCatch(Runnable runnable) {
        try {
            runnable.run(); // covered
        } catch (RuntimeException e) {
            System.out.println("Error"); // missed
        } finally {
            System.out.println("Finally"); // covered
        }
    }
}
