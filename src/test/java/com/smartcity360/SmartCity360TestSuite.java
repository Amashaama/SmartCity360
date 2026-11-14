package com.smartcity360;

import com.smartcity360.tests.TestResult;
import com.smartcity360.tests.TestRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SmartCity360TestSuite {

    @Test
    @DisplayName("Verify all 36 GoF design pattern test cases pass without regressions")
    void verifyAllTestsPass() {
        List<TestResult> results = TestRunner.runAll();
        Assertions.assertFalse(results.isEmpty(), "Test results should not be empty");

        for (TestResult r : results) {
            Assertions.assertTrue(r.isPassed(),
                    "Test failed: [" + r.getTestId() + "] " + r.getDescription() +
                    " | Expected: " + r.getExpected() + " | Actual: " + r.getActual());
        }
    }
}

