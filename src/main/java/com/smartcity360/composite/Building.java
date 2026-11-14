package com.smartcity360.composite;


public class Building implements CityZone {

    private static final String POWER_ON_TEXT = "ON";
    private static final String POWER_OFF_TEXT = "OFF";

    private long id;                   
    private String buildingName;
    private int powerUsage;            
    private boolean isPowerOn;

    public Building(String buildingName, int powerUsage) {
        this.buildingName = buildingName;
        this.powerUsage = powerUsage;
        this.isPowerOn = true;        
    }

    @Override
    public void shutdownPower() {
        isPowerOn = false;
    }

    @Override
    public void restorePower() {
        isPowerOn = true;
    }

    @Override
    public String reportUsage() {
        String state = isPowerOn ? POWER_ON_TEXT : POWER_OFF_TEXT;
        return "Building " + buildingName + ": " + getPowerUsage() + " kWh, power " + state;
    }

    /** A building with its power off uses nothing. */
    @Override
    public int getPowerUsage() {
        return isPowerOn ? powerUsage : 0;
    }

   
    public int getRatedUsage() {
        return powerUsage;
    }

    public void setPowerUsage(int powerUsage) {
        this.powerUsage = powerUsage;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public boolean isPowerOn() {
        return isPowerOn;
    }

    public void setPowerOn(boolean powerOn) {
        this.isPowerOn = powerOn;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
