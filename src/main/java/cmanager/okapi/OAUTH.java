package cmanager.okapi;

import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1RequestToken;

import cmanager.global.Constants;

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
