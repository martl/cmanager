package cmanager.global;

import java.awt.Toolkit;

/** Compatibility definitions for older Java versions. */
public class Compatibility {

    // Java 10 deprecated the old method.
    public static int SHORTCUT_KEY_MASK = determineShortcutKeyMask();

    private static int determineShortcutKeyMask() {
        final int javaVersion = Compatibility.getJavaVersionMain();
        if (javaVersion >= 10) {
            return Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        }

        System.out.println(
                "Falling back to the deprecated getMenuShortcutKeyMask() method."
                        + "This compatibility function will probably get removed in a later version."
                        + "Please consider upgrading to the latest Java version if possible."
                        + "(At the moment Java 10 should be sufficient, but may lacks security updates.)");
        return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }

    private static int getJavaVersionMain() {
        final String[] parts = System.getProperty("java.version").split("\\.");

        try {
            return Integer.parseInt(parts[0]);
        } catch (NumberFormatException exception) {
            System.out.println("Could not determine Java version.");
            return -1;
        }
    }
}
