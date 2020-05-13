package cmanager.okapi.responses;

import java.util.List;

public class LogSubmissionDocument {

    private Boolean success;
    private String message;
    private String log_uuid;
    private List<String> log_uuids;

    public Boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getLogUuid() {
        return log_uuid;
    }
}
