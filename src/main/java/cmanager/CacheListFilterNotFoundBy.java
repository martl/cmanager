package cmanager;

import cmanager.geo.Geocache;
import cmanager.geo.GeocacheLog;
import java.util.ArrayList;

public class CacheListFilterNotFoundBy extends CacheListFilterModel {

    private static final long serialVersionUID = 5585453135104325357L;

    private ArrayList<String> usernames = new ArrayList<>();

    public CacheListFilterNotFoundBy() {
        super(FILTER_TYPE.SINGLE_FILTER_VALUE);
        lblLeft2.setText("Not Found by: ");
        runDoModelUpdateNow =
                new Runnable() {
                    @Override
                    public void run() {
                        final String input = textField.getText();
                        final String[] parts = input.split(",");
                        usernames = new ArrayList<>();
                        for (final String part : parts) {
                            usernames.add(part.trim().toLowerCase());
                        }
                    }
                };
    }

    @Override
    protected boolean isGood(Geocache g) {
        for (final GeocacheLog log : g.getLogs()) {
            for (final String username : usernames) {
                if (log.isFoundLog() && log.isAuthor(username)) {
                    return false;
                }
            }
        }
        return true;
    }
}
