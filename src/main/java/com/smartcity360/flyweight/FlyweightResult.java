package com.smartcity360.flyweight;

import java.util.List;


public class FlyweightResult {

    private final int lightCount;
    private final int poolSize;
    private final List<StreetLight> sampleLights;
    private final StreetLight firstLight;
    private final StreetLight sameModelLight;
    private final StreetLight differentModelLight;
    private final boolean sameModelShared;
    private final boolean differentModelShared;
    private final double elapsedMillis;
    private final MemoryReport memoryReport;

    public FlyweightResult(int lightCount, int poolSize, List<StreetLight> sampleLights,
                           StreetLight firstLight, StreetLight sameModelLight,
                           StreetLight differentModelLight, boolean sameModelShared,
                           boolean differentModelShared, double elapsedMillis,
                           MemoryReport memoryReport) {
        this.lightCount = lightCount;
        this.poolSize = poolSize;
        this.sampleLights = sampleLights;
        this.firstLight = firstLight;
        this.sameModelLight = sameModelLight;
        this.differentModelLight = differentModelLight;
        this.sameModelShared = sameModelShared;
        this.differentModelShared = differentModelShared;
        this.elapsedMillis = elapsedMillis;
        this.memoryReport = memoryReport;
    }

    public int getLightCount() {
        return lightCount;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public List<StreetLight> getSampleLights() {
        return sampleLights;
    }

    public StreetLight getFirstLight() {
        return firstLight;
    }

    public StreetLight getSameModelLight() {
        return sameModelLight;
    }

    public StreetLight getDifferentModelLight() {
        return differentModelLight;
    }

   
    public boolean isSameModelShared() {
        return sameModelShared;
    }

 
    public boolean isDifferentModelShared() {
        return differentModelShared;
    }

    public double getElapsedMillis() {
        return elapsedMillis;
    }

    public MemoryReport getMemoryReport() {
        return memoryReport;
    }
}

