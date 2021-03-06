package cmanager.oc;

import cmanager.geo.Geocache;
import cmanager.global.Constants;
import java.io.IOException;
import java.util.HashMap;

public class ShadowList {

    private static final String SHADOWLIST_FOLDER = Constants.CACHE_FOLDER + "OC.shadowlist";
    private static final String SHADOWLIST_PATH = SHADOWLIST_FOLDER + "/gc2oc.gz";
    private static final String SHADOWLIST_POSTED_FOLDER =
            Constants.CACHE_FOLDER + "OC.shadowlist.posted";

    public static void updateShadowList() throws IOException {
        // TODO: Enable once the API is working again.
        // delete list if it is older than 1 month
        /*final File file = new File(SHADOWLIST_PATH);
        if (file.exists()) {
            DateTime fileTime = new DateTime(file.lastModified());
            final DateTime now = new DateTime();
            fileTime = fileTime.plusMonths(1);
            if (fileTime.isAfter(now)) {
                return;
            }

            file.delete();
        }

        new File(SHADOWLIST_FOLDER).mkdirs();

        // download list
        final URL url = new URL("https://www.opencaching.de/api/gc2oc.php");
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(SHADOWLIST_PATH);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();*/
    }

    public static ShadowList loadShadowList() throws Throwable {
        final HashMap<String, String> shadowList = new HashMap<>();

        System.out.println(
                "The shadow list retrieval has been disabled temporarily as the API endpoint seems to be broken for now.");
        // TODO: Enable after the GZip archive is valid again.
        /*FileHelper.processFiles(SHADOWLIST_PATH, new InputAction() {
            @Override
            public void process(InputStream is) throws Throwable
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = br.readLine()) != null)
                {
                    String token[] = line.split(",");
                    // Column 2 == "1" means verified by a human
                    if (token[2].equals("1"))
                    {
                        // <GC, OC>
                        shadowList.put(token[0], token[1]);
                    }
                }
            }
        });*/

        return new ShadowList(shadowList);
    }

    private HashMap<String, String> shadowList;

    private ShadowList(HashMap<String, String> shadowList) {
        this.shadowList = shadowList;
    }

    public String getMatchingOCCode(String gcCode) {
        return shadowList.get(gcCode);
    }

    public boolean contains(String gcCode) {
        return shadowList.get(gcCode) != null;
    }

    public void postToShadowList(Geocache gc, Geocache oc) throws Exception {
        // TODO: Enable once the API is working again.
        // do not repost items which are already upstream
        /*if (contains(gc.getCode())) {
            return;
        }

        // do not repost local findings
        final File f = new File(SHADOWLIST_POSTED_FOLDER + "/" + gc.getCode());
        if (f.exists()) {
            return;
        }

        final String url =
                "https://www.opencaching.de/api/gc2oc.php"
                        + "?report=1"
                        + "&ocwp="
                        + oc.getCode()
                        + "&gcwp="
                        + gc.getCode()
                        + "&source="
                        + Constants.APP_NAME
                        + "+"
                        + Version.VERSION;

        // post
        HTTP.get(url);

        // remember our post
        new File(SHADOWLIST_POSTED_FOLDER).mkdirs();
        f.createNewFile();*/
    }
}
