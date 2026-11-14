package com.smartcity360.chain;


public abstract class ComplaintHandler {

   
    public static final String RECEIVED_TEXT = "received the complaint";
    public static final String PASSED_TEXT = "cannot handle this severity, passing it to the next level";
    public static final String RESOLVED_TEXT = "resolved the complaint";
    public static final String END_OF_CHAIN_TEXT = "end of chain reached, nobody could handle it";
    private static final String SEPARATOR = ": ";

    protected ComplaintHandler nextHandler;

    public void setNextHandler(ComplaintHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public void handleComplaint(Complaint complaint) {
        audit(complaint, RECEIVED_TEXT);

        if (canHandle(complaint)) {
            process(complaint);
        } else if (nextHandler != null) {
            audit(complaint, PASSED_TEXT);
            nextHandler.handleComplaint(complaint);
        } else {
            audit(complaint, END_OF_CHAIN_TEXT);
        }
    }

    /** True if this handler is responsible for the complaint's severity. */
    protected abstract boolean canHandle(Complaint complaint);

    /** Display name used in the audit trail and in "handled by". */
    protected abstract String getHandlerName();

    /** Default way to handle: resolve it. The fallback handler overrides this. */
    protected void process(Complaint complaint) {
        audit(complaint, RESOLVED_TEXT);
        complaint.markResolved(getHandlerName());
    }

    protected void audit(Complaint complaint, String text) {
        complaint.addToAuditTrail(getHandlerName() + SEPARATOR + text);
    }
}

