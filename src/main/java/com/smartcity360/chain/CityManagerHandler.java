package com.smartcity360.chain;


public class CityManagerHandler extends ComplaintHandler {

    public static final String NAME = "City Manager";

    @Override
    protected boolean canHandle(Complaint complaint) {
        return complaint.getSeverityLevel() == Complaint.SEVERITY_HIGH;
    }

    @Override
    protected String getHandlerName() {
        return NAME;
    }
}

