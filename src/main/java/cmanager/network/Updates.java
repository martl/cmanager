package cmanager.network;

import cmanager.global.Version;
import cmanager.xml.Element;
import cmanager.xml.Parser;

/**
 * Update checking handlers.
 *
 * <p>Please note that all of the code inside this class is specifically
 * tailored to this project, id est the version string format and the
 * version string of new versions being set as the title of each release.
 * Otherwise this update check might fail with unexpected results.</p>
 */
public class Updates {

    private static Boolean updateAvailable = null;
    private static String newVersion = null;

    public static synchronized boolean updateAvailable_block() {        
        if (updateAvailable == null) {
            try {
                final String url = "https://github.com/FriedrichFroebel/cmanager/releases.atom";
                final String http = HTTP.get(url);

                final Element root = Parser.parse(http);

                final Element child = root.getChild("feed").getChild("entry").getChild("title");
                newVersion = child.getUnescapedBody();

                updateAvailable = Updates.isUpdateAvailable(Version.VERSION, newVersion);
            } catch (Throwable t) {
                // Errors might be due to missing internet connection.
                // This will be called for not parseable version strings as well.
                updateAvailable = false;
            }
        }

        return updateAvailable;
    }

    public static String getNewVersion() {
        return newVersion;
    }

    /**
     * Compare the two version strings to check whether an update is available.
     *
     * @param oldVersion The version string for the old version.
     * @param newVersion The version string for the new version.
     *
     * @return Whether there is an update available or not. Empty new versions will
     *         correspond to no update, as well as newer old versions.
     *
     * @throws NumberFormatException One of the groups does not hold a valid integer
     *                               value.
     */
    private static boolean isUpdateAvailable(String oldVersion, String newVersion)
        throws NumberFormatException {
        // Something went wrong during the download.
        if (newVersion == null || newVersion.isEmpty()) {
            return false;
        }

        // Split the old version name.
        final String[] oldParts = oldVersion.split("\\.");
        final int oldLength = oldParts.length;

        // Split the new version name.
        final String[] newParts = newVersion.split("\\.");
        final int newLength = newParts.length;

        // Determine the maximum split length.
        final int maxLength = Math.max(oldLength, newLength);

        // Compare groupwise.
        for (int i = 0; i < maxLength; i++) {
            final int oldEntry = i < oldLength ? Integer.parseInt(oldParts[i]) : 0;
            final int newEntry = i < newLength ? Integer.parseInt(newParts[i]) : 0;

            if (oldEntry < newEntry) {
                return true;
            }
            if (oldEntry > newEntry) {
                return false;
            }
        }

        return false;
    }
}
