package com.smartcity360.flyweight;


public class StreetLight {

    private final String lightId;
    private final String gpsCoordinates;
    private boolean isTurnedOn;
    private final StreetLightType type;

    public StreetLight(String lightId, String gpsCoordinates, boolean isTurnedOn, StreetLightType type) {
        this.lightId = lightId;
        this.gpsCoordinates = gpsCoordinates;
        this.isTurnedOn = isTurnedOn;
        this.type = type;
    }

    public String getLightId() {
        return lightId;
    }

    public String getGpsCoordinates() {
        return gpsCoordinates;
    }

    public boolean isTurnedOn() {
        return isTurnedOn;
    }

   
    public void setTurnedOn(boolean turnedOn) {
        this.isTurnedOn = turnedOn;
    }

    public StreetLightType getType() {
        return type;
    }
}

