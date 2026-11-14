package com.smartcity360.flyweight;


public class MemoryReport {

    public static final int BYTES_PER_TYPE = 84;        //size of one StreetLightType
    public static final int BYTES_PER_REFERENCE = 8;    // size of one reference to a type
    private static final double PERCENT = 100.0;

    private final int lightCount;
    private final int typeCount;

    public MemoryReport(int lightCount, int typeCount) {
        this.lightCount = lightCount;
        this.typeCount = typeCount;
    }

   
    public long getBytesWithoutFlyweight() {
        return (long) lightCount * BYTES_PER_TYPE;
    }

 
    public long getBytesWithFlyweight() {
        return (long) typeCount * BYTES_PER_TYPE + (long) lightCount * BYTES_PER_REFERENCE;
    }

    public long getBytesSaved() {
        return getBytesWithoutFlyweight() - getBytesWithFlyweight();
    }

    public double getPercentSaved() {
        return getBytesSaved() * PERCENT / getBytesWithoutFlyweight();
    }

    public int getLightCount() {
        return lightCount;
    }

    public int getTypeCount() {
        return typeCount;
    }
}

