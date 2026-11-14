package com.smartcity360.tests;

import com.smartcity360.chain.ChainBuilder;
import com.smartcity360.chain.Complaint;
import com.smartcity360.chain.ComplaintHandler;
import com.smartcity360.composite.Building;
import com.smartcity360.composite.CityBuilder;
import com.smartcity360.composite.CityZone;
import com.smartcity360.composite.CompositeZone;
import com.smartcity360.composite.ScaleDemoResult;
import com.smartcity360.decorator.AuthenticationDecorator;
import com.smartcity360.decorator.CompressionDecorator;
import com.smartcity360.decorator.DataStream;
import com.smartcity360.decorator.EncryptionDecorator;
import com.smartcity360.decorator.SmartMeterStream;
import com.smartcity360.decorator.StreamBuilder;
import com.smartcity360.decorator.StreamDemo;
import com.smartcity360.decorator.StreamOptions;
import com.smartcity360.flyweight.FlyweightDemo;
import com.smartcity360.flyweight.MemoryReport;
import com.smartcity360.flyweight.StreetLight;
import com.smartcity360.flyweight.StreetLightFactory;
import com.smartcity360.flyweight.StreetLightType;
import com.smartcity360.interpreter.AndExpression;
import com.smartcity360.interpreter.Expression;
import com.smartcity360.interpreter.OrExpression;
import com.smartcity360.interpreter.PolicyRules;
import com.smartcity360.interpreter.TerminalExpression;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


public class TestRunner {

    private static final String TOKEN = "abc";
    private static final String WRONG_TOKEN = "zzz";
    private static final String CONTEXT_SEPARATOR = ",";
    private static final String HANDLER_SEPARATOR = ",";
    private static final String AUDIT_SEPARATOR = ":";
    private static final String SETTER_PREFIX = "set";
    private static final String ADD_ZONE_METHOD = "addZone";
    private static final int LINE_WIDTH = 150;

    private final List<TestResult> results = new ArrayList<>();

   
    public static List<TestResult> runAll() {
        TestRunner runner = new TestRunner();
        runner.runDecoratorTests();
        runner.runCompositeTests();
        runner.runChainTests();
        runner.runFlyweightTests();
        runner.runInterpreterTests();
        return runner.results;
    }

    private void check(String id, String description, Object expected, Object actual) {
        results.add(new TestResult(id, description, String.valueOf(expected), String.valueOf(actual)));
    }

    // ---------------------------------------------------------------- Meter Telemetry

    private SmartMeterStream newMeter() {
        return new SmartMeterStream("TEST-METER", "raw");
    }


    private String readOrExceptionName(DataStream stream) {
        try {
            return stream.readData();
        } catch (SecurityException e) {
            return e.getClass().getSimpleName();
        }
    }

    private void runDecoratorTests() {
        SmartMeterStream meter = newMeter();
        new CompressionDecorator(meter).writeData("a   b   c  ");
        check("A1", "Compression alone: removes extra spaces", "a b c", meter.readData());

        meter = newMeter();
        new EncryptionDecorator(meter).writeData("hello");
        check("A2", "Encryption alone: AES-128 Base64 ciphertext is written", EncryptionDecorator.encrypt("hello"), meter.readData());

        meter = newMeter();
        DataStream encrypted = new EncryptionDecorator(meter);
        encrypted.writeData("hello");
        check("A3", "Encryption alone: read reverses the AES encryption", "hello", encrypted.readData());

        meter = newMeter();
        DataStream authenticated = new AuthenticationDecorator(meter, TOKEN);
        authenticated.writeData("data");
        check("A4", "Authentication alone: write attaches the token", "TOKEN:abc|data", meter.readData());
        check("A5", "Authentication alone: read strips the token", "data", authenticated.readData());

        meter = newMeter();
        StreamOptions all = new StreamOptions(true, true, true, TOKEN);
        DataStream secure = StreamBuilder.buildSecureStream(meter, all);
        secure.writeData("a   b");
        check("A6", "All three: compress, then AES encrypt, then attach token",
                "TOKEN:abc|" + EncryptionDecorator.encrypt("a b"), meter.readData());
        check("A7", "All three: round trip returns original without extra spaces", "a b", secure.readData());

        check("A8", "Wrong token: read throws SecurityException", "SecurityException",
                readOrExceptionName(wrongTokenStream(all)));

        meter = newMeter();
        DataStream plain = StreamBuilder.buildSecureStream(meter, new StreamOptions(false, false, false, TOKEN));
        plain.writeData("plain");
        check("A9", "No options selected: data passes through unchanged", "plain", plain.readData());

        StreamOptions verifyOpts = new StreamOptions(true, true, true, "secret123");
        StreamDemo.VerificationResult granted = StreamDemo.verifyToken("M-1", "350 kWh", verifyOpts, "secret123");
        check("A10", "Token verification: correct token grants access and returns reading", "350 kWh", granted.getDecodedData());
        StreamDemo.VerificationResult denied = StreamDemo.verifyToken("M-1", "350 kWh", verifyOpts, "wrong-pass");
        check("A11", "Token verification: wrong token denies access", false, denied.isSuccess());
    }


    private DataStream wrongTokenStream(StreamOptions options) {
        SmartMeterStream meter = newMeter();
        StreamBuilder.buildSecureStream(meter, options).writeData("secret reading");
        StreamOptions wrong = new StreamOptions(
                options.isCompress(), options.isEncrypt(), true, WRONG_TOKEN);
        return StreamBuilder.buildSecureStream(meter, wrong);
    }

    // ---------------------------------------------------------------- Zone Grid Control

    private void runCompositeTests() {
        Building building = new Building("Test Hall", 120);
        check("B1", "Building returns its own power usage", 120, building.getPowerUsage());

        CompositeZone district = new CompositeZone("Test District");
        CompositeZone street = new CompositeZone("Test Street");
        street.addZone(new Building("B1", 100));
        street.addZone(new Building("B2", 200));
        district.addZone(street);
        district.addZone(new Building("B3", 50));
        check("B2", "Nested zones: district total = 100 + 200 + 50", 350, district.getPowerUsage());

        Building first = new Building("S1", 10);
        Building second = new Building("S2", 20);
        Building third = new Building("S3", 30);
        CompositeZone inner = new CompositeZone("Inner");
        inner.addZone(second);
        inner.addZone(third);
        CompositeZone outer = new CompositeZone("Outer");
        outer.addZone(first);
        outer.addZone(inner);
        outer.shutdownPower();
        check("B3", "Shutdown reaches every leaf (isPowerOn of 3 buildings)", "false,false,false",
                first.isPowerOn() + "," + second.isPowerOn() + "," + third.isPowerOn());
        check("B4", "Shutdown makes the zone's total usage 0", 0, outer.getPowerUsage());

        CompositeZone empty = new CompositeZone("Empty");
        empty.addZone(new Building("New", 75));
        check("B5", "addZone adds a child: usage becomes 75", 75, empty.getPowerUsage());

        Building removable = new Building("Old", 40);
        empty.addZone(removable);
        empty.removeZone(removable);
        check("B6", "removeZone removes a child: usage back to 75", 75, empty.getPowerUsage());

        check("B7", "CityZone interface has no addZone method", false, interfaceHasMethod(CityZone.class, ADD_ZONE_METHOD));

        check("B8", "Demo city total matches the hand-calculated sum", expectedUsage(
                CityBuilder.DEMO_DISTRICTS, CityBuilder.DEMO_STREETS_PER_DISTRICT, CityBuilder.DEMO_BUILDINGS_PER_STREET),
                CityBuilder.buildDemoCity().getPowerUsage());

        ScaleDemoResult scale = CityBuilder.runScaleDemo();
        check("B9", "Scale demo (100,000 buildings): total matches hand-calculated sum",
                expectedUsage(CityBuilder.SCALE_DISTRICTS, CityBuilder.SCALE_STREETS_PER_DISTRICT,
                        CityBuilder.SCALE_BUILDINGS_PER_STREET),
                scale.getTotalUsage());
    }

    private boolean interfaceHasMethod(Class<?> type, String methodName) {
        for (Method method : type.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }


    private int expectedUsage(int districts, int streets, int buildings) {
        int total = 0;
        for (int d = 1; d <= districts; d++) {
            for (int s = 1; s <= streets; s++) {
                for (int b = 1; b <= buildings; b++) {
                    total += CityBuilder.usageFor(d, s, b);
                }
            }
        }
        return total;
    }

    // ---------------------------------------------------------------- Citizen Complaints

    private Complaint submit(int severity) {
        ComplaintHandler chain = ChainBuilder.buildChain();
        Complaint complaint = new Complaint("Test complaint", "Test Resident", severity);
        chain.handleComplaint(complaint);
        return complaint;
    }

    private String handledByAndStatus(Complaint complaint) {
        return complaint.getHandledBy() + " / " + complaint.getStatus();
    }


    private String visitedHandlers(Complaint complaint) {
        List<String> names = new ArrayList<>();
        for (String entry : complaint.getAuditTrail()) {
            if (entry.contains(ComplaintHandler.RECEIVED_TEXT)) {
                names.add(entry.substring(0, entry.indexOf(AUDIT_SEPARATOR)));
            }
        }
        return String.join(HANDLER_SEPARATOR, names);
    }

    private void runChainTests() {
        check("C1", "Severity 1 is handled by Local Staff", "Local Staff / Resolved",
                handledByAndStatus(submit(1)));
        check("C2", "Severity 2 is handled by Supervisor", "Supervisor / Resolved",
                handledByAndStatus(submit(2)));
        check("C3", "Severity 3 is handled by City Manager", "City Manager / Resolved",
                handledByAndStatus(submit(3)));
        check("C4", "Severity 0 reaches the fallback handler", "Fallback Handler / requires manual review",
                handledByAndStatus(submit(0)));
        check("C5", "Severity 4 reaches the fallback handler", "Fallback Handler / requires manual review",
                handledByAndStatus(submit(4)));

        check("C6", "Severity 2 audit trail has 4 entries", 4, submit(2).getAuditTrail().size());
        check("C7", "Severity 3 audit trail lists handlers in order", "Local Staff,Supervisor,City Manager",
                visitedHandlers(submit(3)));

        Complaint unhandled = submit(0);
        check("C8", "Severity 0 passes through all 4 handlers", "Local Staff,Supervisor,City Manager,Fallback Handler",
                visitedHandlers(unhandled));
        List<String> trail = unhandled.getAuditTrail();
        check("C9", "Severity 0 last audit entry says manual review", "Fallback Handler: requires manual review",
                trail.get(trail.size() - 1));
    }

    // ---------------------------------------------------------------- Street Lighting

    private void runFlyweightTests() {
        StreetLightFactory.clearPool();
        StreetLightType a = StreetLightFactory.getStreetLightType("LED", "X1", 45);
        StreetLightType b = StreetLightFactory.getStreetLightType("LED", "X1", 45);
        StreetLightType c = StreetLightFactory.getStreetLightType("LED", "X2", 45);
        check("D1", "Same inputs return the same object (==)", true, a == b);
        check("D2", "Different inputs return different objects (==)", false, a == c);

        List<StreetLight> lights = FlyweightDemo.createLights(FlyweightDemo.LIGHT_COUNT);
        check("D3", "50,000 street lights are created", 50_000, lights.size());
        check("D4", "Pool size is 5 after creating 50,000 lights", 5, StreetLightFactory.getPoolSize());
        check("D5", "Two lights of the same model share one type (==)", true,
                lights.get(0).getType() == lights.get(FlyweightDemo.TYPE_COUNT).getType());
        check("D6", "Two lights of different models do not share a type", false,
                lights.get(0).getType() == lights.get(1).getType());

        check("D7", "StreetLightType has no setter methods", 0, countSetters(StreetLightType.class));
        check("D8", "StreetLightType: every field is final", true, allFieldsFinal(StreetLightType.class));

        MemoryReport memory = new MemoryReport(lights.size(), StreetLightFactory.getPoolSize());
        check("D9", "Memory without Flyweight (bytes)", 4_200_000L, memory.getBytesWithoutFlyweight());
        check("D10", "Memory with Flyweight (bytes)", 400_420L, memory.getBytesWithFlyweight());
    }

    private int countSetters(Class<?> type) {
        int count = 0;
        for (Method method : type.getDeclaredMethods()) {
            if (method.getName().startsWith(SETTER_PREFIX)) {
                count++;
            }
        }
        return count;
    }

    private boolean allFieldsFinal(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isFinal(field.getModifiers())) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------- Policy Engine

    private String contextOf(String... keywords) {
        return String.join(CONTEXT_SEPARATOR, keywords);
    }

    private void runInterpreterTests() {
        PolicyRules rules = new PolicyRules();
        Expression noiseRule = rules.getNoiseRule();
        Expression violationRule = rules.getViolationRule();

        check("E1", "AND true: noiseRule with HIGH_NOISE + RESTRICTED_HOURS", true,
                noiseRule.interpret(contextOf(PolicyRules.HIGH_NOISE, PolicyRules.RESTRICTED_HOURS)));
        check("E2", "AND false: noiseRule with only HIGH_NOISE", false,
                noiseRule.interpret(contextOf(PolicyRules.HIGH_NOISE)));
        check("E3", "AND false: noiseRule with only RESTRICTED_HOURS", false,
                noiseRule.interpret(contextOf(PolicyRules.RESTRICTED_HOURS)));

        Expression noiseOrParking = new OrExpression(
                new TerminalExpression(PolicyRules.HIGH_NOISE),
                new TerminalExpression(PolicyRules.ILLEGAL_PARKING));
        check("E4", "OR true: only ILLEGAL_PARKING is present", true,
                noiseOrParking.interpret(contextOf(PolicyRules.ILLEGAL_PARKING)));
        check("E5", "OR false: neither keyword is present", false,
                noiseOrParking.interpret(contextOf(PolicyRules.RESTRICTED_HOURS)));

        check("E6", "Nested rule true: ILLEGAL_PARKING + RESTRICTED_HOURS", true,
                violationRule.interpret(contextOf(PolicyRules.ILLEGAL_PARKING, PolicyRules.RESTRICTED_HOURS)));
        check("E7", "Nested rule false: parking but not in restricted hours", false,
                violationRule.interpret(contextOf(PolicyRules.ILLEGAL_PARKING)));
        check("E8", "Rule tree text of violationRule",
                "((HIGH_NOISE OR ILLEGAL_PARKING) AND RESTRICTED_HOURS)", violationRule.toString());

        Expression simpleAnd = new AndExpression(
                new TerminalExpression(PolicyRules.HIGH_NOISE),
                new TerminalExpression(PolicyRules.ILLEGAL_PARKING));
        check("E9", "AND false on an empty context", false, simpleAnd.interpret(""));

        Expression criticalEmergency = rules.getCriticalEmergencyRule();
        check("E10", "Critical emergency: EMERGENCY_ZONE + HAZARDOUS_SMOKE", true,
                criticalEmergency.interpret(contextOf(PolicyRules.EMERGENCY_ZONE, PolicyRules.HAZARDOUS_SMOKE)));
    }

    // ---------------------------------------------------------------- Console


    public static void main(String[] args) {
        List<TestResult> results = runAll();
        String rowFormat = "%-5s | %-66s | %-42s | %-42s | %s%n";

        System.out.printf(rowFormat, "ID", "Description", "Expected", "Actual", "Result");
        System.out.println("-".repeat(LINE_WIDTH));

        int passed = 0;
        for (TestResult r : results) {
            System.out.printf(rowFormat, r.getTestId(), r.getDescription(),
                    r.getExpected(), r.getActual(), r.getVerdict());
            if (r.isPassed()) {
                passed++;
            }
        }

        System.out.println("-".repeat(LINE_WIDTH));
        System.out.println(passed + " of " + results.size() + " tests passed");
        if (passed != results.size()) {
            System.exit(1);
        }
    }
}

