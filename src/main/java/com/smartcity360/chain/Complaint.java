package com.smartcity360.chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Complaint {

    
    public static final int SEVERITY_LOW = 1;
    public static final int SEVERITY_MEDIUM = 2;
    public static final int SEVERITY_HIGH = 3;

    public static final String NOT_HANDLED = "Nobody yet";
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_RESOLVED = "Resolved";
    public static final String MANUAL_REVIEW_TEXT = "requires manual review";

    private final String title;
    private final String residentName;
    private final int severityLevel;
    private final List<String> auditTrail = new ArrayList<>();

    private String handledBy = NOT_HANDLED;
    private String status = STATUS_PENDING;

    public Complaint(String title, String residentName, int severityLevel) {
        this.title = title;
        this.residentName = residentName;
        this.severityLevel = severityLevel;
    }

    public void addToAuditTrail(String entry) {
        auditTrail.add(entry);
    }

   
    public List<String> getAuditTrail() {
        return Collections.unmodifiableList(auditTrail);
    }

    public void markResolved(String handlerName) {
        this.handledBy = handlerName;
        this.status = STATUS_RESOLVED;
    }

    public void flagForManualReview(String handlerName) {
        this.handledBy = handlerName;
        this.status = MANUAL_REVIEW_TEXT;
    }

    public String getTitle() {
        return title;
    }

    public String getResidentName() {
        return residentName;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }

    public String getHandledBy() {
        return handledBy;
    }

    public String getStatus() {
        return status;
    }
}

