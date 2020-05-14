package cmanager.global;

public class Constants
{
    public static final String APP_NAME = "cmanager";

    public static final String CACHE_FOLDER =
        System.getProperty("user.home") + "/." + APP_NAME + "/cache/";

    public final static String HTTP_USER_AGENT = APP_NAME + " " + Version.VERSION;

    // Allow changing the site with one single variable.
    // This is currently used for manual testing.
    public final static String SITE_BASE = "https://www.opencaching.de/";
    public final static String OKAPI_BASE = SITE_BASE + "okapi";
    public final static String OKAPI_SERVICE_BASE = OKAPI_BASE + "/services";
}
