package cmanager.okapi.responses;

public class UnexpectedLogStatus extends Exception {

    private static final long serialVersionUID = -1132973286480626832L;

    private String responseMessage;

    public UnexpectedLogStatus(String responseMessage) {
        super("Unexpected log status");
        this.responseMessage = responseMessage;
    }

    public String getResponseMessage() {
        return responseMessage;
    }
}
