package cmanager.okapi;

import cmanager.MalFormedException;
import cmanager.geo.Coordinate;
import cmanager.geo.Geocache;
import cmanager.geo.GeocacheLog;
import cmanager.global.Constants;
import cmanager.gui.ExceptionPanel;
import cmanager.network.ApacheHTTP;
import cmanager.network.ApacheHTTP.HttpResponse;
import cmanager.network.UnexpectedStatusCode;
import cmanager.okapi.responses.CacheDetailsDocument;
import cmanager.okapi.responses.CacheDocument;
import cmanager.okapi.responses.CachesAroundDocument;
import cmanager.okapi.responses.ErrorDocument;
import cmanager.okapi.responses.FoundStatusDocument;
import cmanager.okapi.responses.LogSubmissionDocument;
import cmanager.okapi.responses.UUIDDocument;
import cmanager.okapi.responses.UnexpectedLogStatus;
import cmanager.okapi.responses.UsernameDocument;
import cmanager.xml.Element;
import cmanager.xml.Parser;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

public class OKAPI {

    private static final String CONSUMER_API_KEY = ConsumerKeys.get_CONSUMER_API_KEY();
    private static final String CONSUMER_SECRET_KEY = ConsumerKeys.get_CONSUMER_SECRET_KEY();
    private static final String BASE_URL = Constants.OKAPI_SERVICE_BASE;

    private static ApacheHTTP httpClient = new ApacheHTTP();

    public static String usernameToUUID(String username) throws Exception {
        final String url =
                BASE_URL
                        + "/users/by_username"
                        + "?consumer_key="
                        + CONSUMER_API_KEY
                        + "&username="
                        + URLEncoder.encode(username, "UTF-8")
                        + "&fields=uuid";

        final HttpResponse response = httpClient.get(url);
        final String http = response.getBody();

        if (response.getStatusCode() != 200) {
            final ErrorDocument okapiError = new Gson().fromJson(http, ErrorDocument.class);
            if (okapiError.getParameter().equals("username")) {
                return null;
            } else {
                throw new UnexpectedStatusCode(response.getStatusCode(), http);
            }
        }

        final UUIDDocument document = new Gson().fromJson(http, UUIDDocument.class);
        return document.getUuid();
    }

    public static Geocache getCache(String code) throws Exception {
        final String url =
                BASE_URL
                        + "/caches/geocache"
                        + "?consumer_key="
                        + CONSUMER_API_KEY
                        + "&cache_code="
                        + code
                        + "&fields="
                        + URLEncoder.encode(
                                "code|name|location|type|gc_code|difficulty|terrain|status",
                                "UTF-8");

        final HttpResponse response = httpClient.get(url);
        final String http = response.getBody();

        if (response.getStatusCode() != 200) {
            final ErrorDocument okapiError = new Gson().fromJson(http, ErrorDocument.class);
            if (okapiError.getParameter().equals("cache_code")) {
                return null;
            } else {
                throw new UnexpectedStatusCode(response.getStatusCode(), http);
            }
        }

        final CacheDocument document = new Gson().fromJson(http, CacheDocument.class);
        if (document == null) {
            return null;
        }

        Coordinate coordinate = null;
        if (document.getLocation() != null) {
            final String[] parts = document.getLocation().split("\\|");
            coordinate = new Coordinate(parts[0], parts[1]);
        }

        final Geocache g =
                new Geocache(
                        code,
                        document.getName(),
                        coordinate,
                        document.getDifficulty(),
                        document.getTerrain(),
                        document.getType());
        g.setCodeGC(document.getGc_code());

        final String status = document.getStatus();
        if (status != null) {
            if (status.equals("Archived")) {
                g.setAvailable(false);
                g.setArchived(true);
            } else if (status.equals("Temporarily unavailable")) {
                g.setAvailable(false);
                g.setArchived(false);
            } else if (status.equals("Available")) {
                g.setAvailable(true);
                g.setArchived(false);
            }
        }

        return g;
    }

    public static Geocache getCacheBuffered(String code, ArrayList<Geocache> okapiRuntimeCache)
            throws Exception {
        synchronized (okapiRuntimeCache) {
            final int index = Collections.binarySearch(okapiRuntimeCache, code);
            if (index >= 0) {
                return okapiRuntimeCache.get(index);
            }
        }

        final Geocache g = getCache(code);
        if (g != null) {
            synchronized (okapiRuntimeCache) {
                okapiRuntimeCache.add(g);
                Collections.sort(
                        okapiRuntimeCache,
                        new Comparator<Geocache>() {
                            public int compare(Geocache o1, Geocache o2) {
                                return o1.getCode().compareTo(o2.getCode());
                            }
                        });
            }
        }
        return g;
    }

    public static Geocache completeCacheDetails(Geocache g) throws Exception {
        final String url =
                BASE_URL
                        + "/caches/geocache"
                        + "?consumer_key="
                        + CONSUMER_API_KEY
                        + "&cache_code="
                        + g.getCode()
                        + "&fields="
                        + URLEncoder.encode(
                                "size2|short_description|description|owner|hint2|req_passwd",
                                "UTF-8");

        final HttpResponse response = httpClient.get(url);
        final String http = response.getBody();

        if (response.getStatusCode() != 200) {
            final ErrorDocument okapiError = new Gson().fromJson(http, ErrorDocument.class);
            if (okapiError.getParameter().equals("cache_code")) {
                return null;
            } else {
                throw new UnexpectedStatusCode(response.getStatusCode(), http);
            }
        }

        final CacheDetailsDocument document = new Gson().fromJson(http, CacheDetailsDocument.class);

        g.setContainer(document.getSize2());
        g.setListing_short(document.getShort_description());
        g.setListing(document.getDescription());
        g.setOwner(document.getOwnerUsername());
        g.setHint(document.getHint2());
        g.setRequiresPassword(document.doesRequirePassword());

        return g;
    }

    public interface RequestAuthorizationCallbackI {
        public void redirectUrlToUser(String authUrl);

        public String getPin();
    }

    private static OAuth10aService getOAuthService() {
        return new ServiceBuilder(CONSUMER_API_KEY)
                .apiSecret(CONSUMER_SECRET_KEY)
                .build(new OAUTH());
    }

    public static OAuth1AccessToken requestAuthorization(RequestAuthorizationCallbackI callback)
            throws IOException, InterruptedException, ExecutionException {
        // Step One: Create the OAuthService object
        final OAuth10aService service = getOAuthService();

        // Step Two: Get the request token
        final OAuth1RequestToken requestToken = service.getRequestToken();

        // Step Three: Making the user validate your request token
        final String authUrl = service.getAuthorizationUrl(requestToken);
        callback.redirectUrlToUser(authUrl);

        final String pin = callback.getPin();
        if (pin == null) {
            return null;
        }

        // Step Four: Get the access Token
        final OAuth1AccessToken accessToken =
                service.getAccessToken(requestToken, pin); // the requestToken you had from step 2

        return accessToken;
    }

    public interface TokenProviderI {
        public OAuth1AccessToken getOkapiToken();
    }

    private static String authedHttpGet(final TokenProviderI tp, final String url)
            throws InterruptedException, ExecutionException, IOException {
        final OAuth10aService service = getOAuthService();
        final OAuthRequest request = new OAuthRequest(Verb.GET, url);
        service.signRequest(tp.getOkapiToken(), request); // the access token from step 4
        final Response response = service.execute(request);
        return response.getBody();
    }

    public static ArrayList<Geocache> getCachesAround(
            TokenProviderI tp,
            String excludeUUID,
            Geocache g,
            double searchRadius,
            ArrayList<Geocache> okapiRuntimeCache)
            throws Exception {
        final Coordinate c = g.getCoordinate();
        return getCachesAround(
                tp, excludeUUID, c.getLat(), c.getLon(), searchRadius, okapiRuntimeCache);
    }

    public static ArrayList<Geocache> getCachesAround(
            TokenProviderI tp,
            String excludeUUID,
            Double lat,
            Double lon,
            Double searchRadius,
            ArrayList<Geocache> okapiCacheDetailsCache)
            throws Exception {
        final boolean useOAuth = tp != null && excludeUUID != null;
        final String url =
                BASE_URL
                        + "/caches/search/nearest"
                        + "?consumer_key="
                        + CONSUMER_API_KEY
                        + "&center="
                        + URLEncoder.encode(lat.toString() + "|" + lon.toString(), "UTF-8")
                        + "&radius="
                        + searchRadius.toString()
                        + "&status="
                        + URLEncoder.encode("Available|Temporarily unavailable|Archived", "UTF-8")
                        + "&limit=500"
                        + (useOAuth ? "&ignored_status=notignored_only" : "")
                        + (useOAuth ? "&not_found_by=" + excludeUUID : "");

        String http;
        if (useOAuth) {
            http = authedHttpGet(tp, url);
        } else {
            final HttpResponse response = httpClient.get(url);
            http = response.getBody();

            if (response.getStatusCode() != 200) {
                throw new UnexpectedStatusCode(response.getStatusCode(), http);
            }
        }

        final CachesAroundDocument document = new Gson().fromJson(http, CachesAroundDocument.class);
        if (document == null) {
            return null;
        }

        ArrayList<Geocache> caches = new ArrayList<Geocache>();
        for (String code : document.getResults()) {
            try {
                final Geocache g = getCacheBuffered(code, okapiCacheDetailsCache);
                if (g != null) {
                    caches.add(g);
                }
            } catch (MalFormedException ex) {
                ExceptionPanel.display(ex);
            }
        }
        return caches;
    }

    public static void updateFoundStatus(TokenProviderI tp, Geocache oc)
            throws MalFormedException, IOException, InterruptedException, ExecutionException {
        if (tp == null) {
            return;
        }

        final String url =
                BASE_URL
                        + "/caches/geocache"
                        + "?consumer_key="
                        + CONSUMER_API_KEY
                        + "&cache_code="
                        + oc.getCode()
                        + "&fields=is_found";
        final String http = authedHttpGet(tp, url);

        final FoundStatusDocument document = new Gson().fromJson(http, FoundStatusDocument.class);
        oc.setIsFound(document.isIs_found());
    }

    public static String getUUID(TokenProviderI tp)
            throws MalFormedException, IOException, InterruptedException, ExecutionException {
        final String url = BASE_URL + "/users/user" + "?fields=uuid";
        final String http = authedHttpGet(tp, url);

        final UUIDDocument document = new Gson().fromJson(http, UUIDDocument.class);
        if (document == null) {
            return null;
        }
        return document.getUuid();
    }

    public static String getUsername(TokenProviderI tp)
            throws MalFormedException, IOException, InterruptedException, ExecutionException {
        final String url = BASE_URL + "/users/user" + "?fields=username";
        final String http = authedHttpGet(tp, url);

        final UsernameDocument document = new Gson().fromJson(http, UsernameDocument.class);
        if (document == null) {
            return null;
        }
        return document.getUsername();
    }

    public static void postLog(TokenProviderI tp, Geocache cache, GeocacheLog log)
            throws MalFormedException, InterruptedException, ExecutionException, IOException,
                    UnexpectedLogStatus {
        String url =
                BASE_URL
                        + "/logs/submit"
                        + "?format=json"
                        + "&cache_code="
                        + URLEncoder.encode(cache.getCode(), "UTF-8")
                        + "&logtype="
                        + URLEncoder.encode(log.getOkapiType(cache), "UTF-8")
                        + "&comment="
                        + URLEncoder.encode(log.getText(), "UTF-8")
                        + "&when="
                        + URLEncoder.encode(log.getDateStrISO8601NoTime(), "UTF-8");

        /*if (cache.doesRequirePassword()) {
            url += "&password=" + URLEncoder.encode(log.getPassword(), "UTF-8");
        }*/

        // System.out.println(url);

        final String response = authedHttpGet(tp, url);

        // TODO: We might want to handle another problem here as well, although this should not be
        // this common.
        // Currently this is done by catching the NPE below
        // {"error":{"developer_message":"Parameter 'logtype' has invalid value: 'Webcam Photo
        // Taken' in not a valid logtype
        // code.","reason_stack":["bad_request","invalid_parameter"],"status":400,"parameter":"logtype","whats_wrong_about_it":"'Webcam Photo Taken' in not a valid logtype code.","more_info":"https:\/\/www.opencaching.de\/okapi\/introduction.html#errors"}}

        // Retrieve the response document.
        final LogSubmissionDocument document =
                new Gson().fromJson(response, LogSubmissionDocument.class);

        // The document itself is null.
        if (document == null) {
            throw new NullPointerException(
                    "Problems with handling posted log. Response document is null.");
        }

        // Check success status.
        try {
            if (!document.isSuccess()) {
                throw new UnexpectedLogStatus(document.getMessage());
            }
        } catch (NullPointerException exception) {
            // When the OKAPI reports a request error, this will lead to a NPE.
            System.out.println(response);
            throw new NullPointerException(
                    "Could not submit log. Please open an issue for this and add the terminal output to it.");
        }
    }

    public static Coordinate getHomeCoordinates(TokenProviderI tp)
            throws MalFormedException, IOException, InterruptedException, ExecutionException {
        final String uuid = getUUID(tp);

        final String url =
                BASE_URL
                        + "/users/user"
                        + "?format=xmlmap2"
                        + "&fields=home_location"
                        + "&user_uuid="
                        + uuid;
        final String http = authedHttpGet(tp, url);

        // <object><string key="home_location">53.047117|9.608</string></object>
        final Element root = Parser.parse(http);
        for (final Element e : root.getChild("object").getChildren()) {
            if (e.attrIs("key", "home_location")) {
                final String[] parts = e.getUnescapedBody().split("\\|");
                return new Coordinate(parts[0], parts[1]);
            }
        }

        return null;
    }
}
