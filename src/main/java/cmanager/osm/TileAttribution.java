package cmanager.osm;

import java.awt.Image;
import org.openstreetmap.gui.jmapviewer.interfaces.Attributed;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

/** Attribution data provider for the OSM tiles used by the application. */
// Based upon
// https://trac.openstreetmap.org/browser/subversion/applications/viewer/jmapviewer/src/org/openstreetmap/gui/jmapviewer/interfaces/Attributed.java
public class TileAttribution implements Attributed {

    /**
     * Whether the tile source requires attribution in text or image form.
     *
     * @return Always true, as we have to attribute the tiles.
     */
    @Override
    public boolean requiresAttribution() {
        return true;
    }

    /**
     * The URL to open when the user clicks the attribution text.
     *
     * @return The copyright URL of the OpenStreetMap project.
     */
    @Override
    public String getAttributionLinkURL() {
        return "https://www.openstreetmap.org/copyright";
    }

    /**
     * The URL for the attribution image.
     *
     * @return Always null, as we do not have an image for it.
     */
    @Override
    public Image getAttributionImage() {
        return null;
    }

    /**
     * The URL to open when the user clicks the attribution image.
     *
     * @return Always null, but should not matter as we do not use an attribution image.
     */
    @Override
    public String getAttributionImageURL() {
        return null;
    }

    /**
     * The attribution "Terms of Use" text.
     *
     * @return Always null, which leads to the default text being used.
     */
    @Override
    public String getTermsOfUseText() {
        return null;
    }

    /**
     * The URL to open when the user clicks the attribution "Terms of Use" text.
     *
     * @return The copyright URL of the OpenStreetMap project.
     */
    @Override
    public String getTermsOfUseURL() {
        return "https://www.openstreetmap.org/copyright";
    }

    /**
     * Get the text with the attribution.
     *
     * @param zoom The zoom level for the view. This is unused.
     * @param topLeft The top left of the bounding box for attribution. This is unused.
     * @param botRight The bottom right of the bounding box for attribution. This is unused.
     * @return The attribution text as per OpenStreetMap copyright page.
     */
    @Override
    public String getAttributionText(int zoom, ICoordinate topLeft, ICoordinate botRight) {
        return "\u00a9 OpenStreetMap contributors ";
    }
}
