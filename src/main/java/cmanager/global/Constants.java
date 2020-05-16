package cmanager.global;

public class Constants {

    public static final String APP_NAME = "cmanager";

    public static final String CACHE_FOLDER =
            System.getProperty("user.home") + "/." + APP_NAME + "/cache/";

    public static final String HTTP_USER_AGENT = APP_NAME + " " + Version.VERSION;

    // Allow changing the site with one single variable.
    // This is currently used for manual testing.
    public static final String SITE_BASE = "https://www.opencaching.de/";
    public static final String OKAPI_BASE = SITE_BASE + "okapi";
    public static final String OKAPI_SERVICE_BASE = OKAPI_BASE + "/services";
}
