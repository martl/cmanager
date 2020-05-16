package cmanager.okapi;

import cmanager.global.Constants;
import com.github.scribejava.core.builder.api.DefaultApi10a;

public class OAUTH extends DefaultApi10a {

    @Override
    public String getAccessTokenEndpoint() {
        return Constants.OKAPI_SERVICE_BASE + "/oauth/access_token";
    }

    @Override
    public String getRequestTokenEndpoint() {
        return Constants.OKAPI_SERVICE_BASE + "/oauth/request_token";
    }

    @Override
    public String getAuthorizationBaseUrl() {
        return Constants.OKAPI_SERVICE_BASE + "/oauth/authorize";
    }
}
