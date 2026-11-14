package com.smartcity360.chain;

public class FallbackHandler extends ComplaintHandler {

    public static final String NAME = "Fallback Handler";

 
    @Override
    protected boolean canHandle(Complaint complaint) {
        return true;
    }

    @Override
    protected String getHandlerName() {
        return NAME;
    }

    
    @Override
    protected void process(Complaint complaint) {
        audit(complaint, Complaint.MANUAL_REVIEW_TEXT);
        complaint.flagForManualReview(NAME);
    }
}

