package com.smartcity360.composite;


public class ScaleDemoResult {

    private final int districts;
    private final int streetsPerDistrict;
    private final int buildingsPerStreet;
    private final int buildingCount;
    private final int totalUsage;
    private final double buildMillis;
    private final double traversalMillis;

    public ScaleDemoResult(int districts, int streetsPerDistrict, int buildingsPerStreet,
                           int buildingCount, int totalUsage,
                           double buildMillis, double traversalMillis) {
        this.districts = districts;
        this.streetsPerDistrict = streetsPerDistrict;
        this.buildingsPerStreet = buildingsPerStreet;
        this.buildingCount = buildingCount;
        this.totalUsage = totalUsage;
        this.buildMillis = buildMillis;
        this.traversalMillis = traversalMillis;
    }

    public int getDistricts() {
        return districts;
    }

    public int getStreetsPerDistrict() {
        return streetsPerDistrict;
    }

    public int getBuildingsPerStreet() {
        return buildingsPerStreet;
    }

    public int getBuildingCount() {
        return buildingCount;
    }

    public int getTotalUsage() {
        return totalUsage;
    }

    public double getBuildMillis() {
        return buildMillis;
    }

    public double getTraversalMillis() {
        return traversalMillis;
    }
}

