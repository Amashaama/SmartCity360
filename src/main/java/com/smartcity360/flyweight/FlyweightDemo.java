package com.smartcity360.flyweight;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public final class FlyweightDemo {

    public static final int LIGHT_COUNT = 50_000;
    private static final int SAMPLE_SIZE = 10;

    // The 5 light models 
    private static final String[] TYPE_NAMES =
            {"LED-Standard", "LED-Bright", "Sodium-Classic", "Solar-Eco", "Halogen-Old"};
    private static final String[] BULB_MODELS =
            {"Philips-X200", "Osram-B300", "GE-S100", "Sun-E50", "Lumi-H75"};
    private static final int[] WATTAGES = {45, 60, 70, 30, 90};
    public static final int TYPE_COUNT = TYPE_NAMES.length;

 
    private static final double START_LATITUDE = 40.7000;
    private static final double START_LONGITUDE = -74.0200;
    private static final double GRID_STEP = 0.0005;
    private static final int LIGHTS_PER_GRID_ROW = 250;
    private static final int EVERY_OTHER = 2;
    private static final double NANOS_PER_MILLI = 1_000_000.0;

    private FlyweightDemo() {
        
    }

 
    public static List<StreetLight> createLights(int count) {
        StreetLightFactory.clearPool();  
        List<StreetLight> lights = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int typeIndex = i % TYPE_COUNT;
            StreetLightType type = StreetLightFactory.getStreetLightType(
                    TYPE_NAMES[typeIndex], BULB_MODELS[typeIndex], WATTAGES[typeIndex]);
            lights.add(new StreetLight("SL-" + (i + 1), coordinatesFor(i), i % EVERY_OTHER == 0, type));
        }
        return lights;
    }

  
    public static FlyweightResult run() {
        long start = System.nanoTime();
        List<StreetLight> lights = createLights(LIGHT_COUNT);
        double elapsedMillis = (System.nanoTime() - start) / NANOS_PER_MILLI;

        StreetLight first = lights.get(0);
        StreetLight sameModel = lights.get(TYPE_COUNT);   
        StreetLight differentModel = lights.get(1);     

        boolean sameShared = first.getType() == sameModel.getType();
        boolean differentShared = first.getType() == differentModel.getType();
        int poolSize = StreetLightFactory.getPoolSize();

        return new FlyweightResult(lights.size(), poolSize,
                new ArrayList<>(lights.subList(0, SAMPLE_SIZE)),
                first, sameModel, differentModel, sameShared, differentShared, elapsedMillis,
                new MemoryReport(lights.size(), poolSize));
    }

    private static String coordinatesFor(int index) {
        double latitude = START_LATITUDE + (index / LIGHTS_PER_GRID_ROW) * GRID_STEP;
        double longitude = START_LONGITUDE + (index % LIGHTS_PER_GRID_ROW) * GRID_STEP;
        return String.format(Locale.US, "%.4f, %.4f", latitude, longitude);
    }
}

