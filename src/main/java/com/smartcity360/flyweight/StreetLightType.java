package com.smartcity360.flyweight;


public final class StreetLightType {

    private final String typeName;
    private final String bulbModel;
    private final int wattage;

    StreetLightType(String typeName, String bulbModel, int wattage) {
        this.typeName = typeName;
        this.bulbModel = bulbModel;
        this.wattage = wattage;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getBulbModel() {
        return bulbModel;
    }

    public int getWattage() {
        return wattage;
    }
}

