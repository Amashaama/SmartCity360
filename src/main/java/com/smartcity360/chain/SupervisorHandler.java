package com.smartcity360.chain;


public class SupervisorHandler extends ComplaintHandler {

    public static final String NAME = "Supervisor";

    @Override
    protected boolean canHandle(Complaint complaint) {
        return complaint.getSeverityLevel() == Complaint.SEVERITY_MEDIUM;
    }

    @Override
    protected String getHandlerName() {
        return NAME;
    }
}

