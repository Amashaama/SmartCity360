package com.smartcity360.interpreter;

import java.util.List;

/**
 * SmartCity360 City Rules are here 
 */
public class PolicyRules {

  
    public static final String HIGH_NOISE = "HIGH_NOISE";
    public static final String RESTRICTED_HOURS = "RESTRICTED_HOURS";
    public static final String ILLEGAL_PARKING = "ILLEGAL_PARKING";
    public static final String HAZARDOUS_SMOKE = "HAZARDOUS_SMOKE";
    public static final String EMERGENCY_ZONE = "EMERGENCY_ZONE";
    public static final List<String> ALL_KEYWORDS =
            List.of(HIGH_NOISE, RESTRICTED_HOURS, ILLEGAL_PARKING, HAZARDOUS_SMOKE, EMERGENCY_ZONE);

    // noiseRule = HIGH_NOISE AND RESTRICTED_HOURS
    private final Expression noiseRule = new AndExpression(
            new TerminalExpression(HIGH_NOISE),
            new TerminalExpression(RESTRICTED_HOURS));

    // violationRule = (HIGH_NOISE OR ILLEGAL_PARKING) AND RESTRICTED_HOURS
    private final Expression violationRule = new AndExpression(
            new OrExpression(
                    new TerminalExpression(HIGH_NOISE),
                    new TerminalExpression(ILLEGAL_PARKING)),
            new TerminalExpression(RESTRICTED_HOURS));

    // criticalEmergencyRule = EMERGENCY_ZONE AND (HAZARDOUS_SMOKE OR ILLEGAL_PARKING)
    private final Expression criticalEmergencyRule = new AndExpression(
            new TerminalExpression(EMERGENCY_ZONE),
            new OrExpression(
                    new TerminalExpression(HAZARDOUS_SMOKE),
                    new TerminalExpression(ILLEGAL_PARKING)));

    public Expression getNoiseRule() {
        return noiseRule;
    }

    public Expression getViolationRule() {
        return violationRule;
    }

    public Expression getCriticalEmergencyRule() {
        return criticalEmergencyRule;
    }
}

