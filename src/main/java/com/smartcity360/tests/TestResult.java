package com.smartcity360.tests;


public class TestResult {

    public static final String PASS = "PASS";
    public static final String FAIL = "FAIL";

    private final String testId;
    private final String description;
    private final String expected;
    private final String actual;

    public TestResult(String testId, String description, String expected, String actual) {
        this.testId = testId;
        this.description = description;
        this.expected = expected;
        this.actual = actual;
    }

    public String getTestId() {
        return testId;
    }

    public String getDescription() {
        return description;
    }

    public String getExpected() {
        return expected;
    }

    public String getActual() {
        return actual;
    }

    public boolean isPassed() {
        return expected.equals(actual);
    }

   
    public String getVerdict() {
        return isPassed() ? PASS : FAIL;
    }
}

