package com.smartcity360.chain;


public class LocalStaffHandler extends ComplaintHandler {

    public static final String NAME = "Local Staff";

    @Override
    protected boolean canHandle(Complaint complaint) {
        return complaint.getSeverityLevel() == Complaint.SEVERITY_LOW;
    }

    @Override
    protected String getHandlerName() {
        return NAME;
    }
}

