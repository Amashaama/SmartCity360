package com.smartcity360.composite;


public final class CityBuilder {

    public static final String CITY_NAME = "SmartCity 360";

    // Small demo city: 3 x 3 x 4 = 36 buildings.
    public static final int DEMO_DISTRICTS = 3;
    public static final int DEMO_STREETS_PER_DISTRICT = 3;
    public static final int DEMO_BUILDINGS_PER_STREET = 4;

    // Scale demo: 10 x 20 x 50 = 100,000 buildings.
    public static final int SCALE_DISTRICTS = 10;
    public static final int SCALE_STREETS_PER_DISTRICT = 20;
    public static final int SCALE_BUILDINGS_PER_STREET = 50;

    // Building usage is predictable (not random) so results can be checked by hand.
    private static final int BASE_USAGE_KWH = 100;
    private static final int USAGE_STEP_KWH = 10;
    private static final int USAGE_STEPS = 8;

    private static final double NANOS_PER_MILLI = 1_000_000.0;

    private CityBuilder() {
        
    }

    public static CompositeZone buildDemoCity() {
        return buildCity(DEMO_DISTRICTS, DEMO_STREETS_PER_DISTRICT, DEMO_BUILDINGS_PER_STREET);
    }

    /** City -> districts -> streets -> buildings */
    public static CompositeZone buildCity(int districts, int streetsPerDistrict, int buildingsPerStreet) {
        CompositeZone city = new CompositeZone(CITY_NAME);
        for (int d = 1; d <= districts; d++) {
            CompositeZone district = new CompositeZone("District " + d);
            for (int s = 1; s <= streetsPerDistrict; s++) {
                CompositeZone street = new CompositeZone("Street " + d + "-" + s);
                for (int b = 1; b <= buildingsPerStreet; b++) {
                    street.addZone(new Building("B" + d + "-" + s + "-" + b, usageFor(d, s, b)));
                }
                district.addZone(street);
            }
            city.addZone(district);
        }
        return city;
    }

 
    public static int usageFor(int districtNo, int streetNo, int buildingNo) {
        int step = (districtNo + streetNo + buildingNo) % USAGE_STEPS;
        return BASE_USAGE_KWH + step * USAGE_STEP_KWH;
    }


    public static ScaleDemoResult runScaleDemo() {
        long buildStart = System.nanoTime();
        CompositeZone bigCity = buildCity(
                SCALE_DISTRICTS, SCALE_STREETS_PER_DISTRICT, SCALE_BUILDINGS_PER_STREET);
        double buildMillis = (System.nanoTime() - buildStart) / NANOS_PER_MILLI;

        long traverseStart = System.nanoTime();
        int totalUsage = bigCity.getPowerUsage();
        double traversalMillis = (System.nanoTime() - traverseStart) / NANOS_PER_MILLI;

        int buildingCount = SCALE_DISTRICTS * SCALE_STREETS_PER_DISTRICT * SCALE_BUILDINGS_PER_STREET;
        return new ScaleDemoResult(SCALE_DISTRICTS, SCALE_STREETS_PER_DISTRICT,
                SCALE_BUILDINGS_PER_STREET, buildingCount, totalUsage, buildMillis, traversalMillis);
    }
}

