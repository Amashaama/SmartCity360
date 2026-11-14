package com.smartcity360.decorator;


public class SmartMeterStream implements DataStream {

    private final String meterId;
    private final String rawReading;

    // The last data written... Before any write,.
    private String storedData;

    public SmartMeterStream(String meterId, String rawReading) {
        this.meterId = meterId;
        this.rawReading = rawReading;
        this.storedData = rawReading;
    }

    @Override
    public String readData() {
        return storedData;
    }

    @Override
    public void writeData(String data) {
        this.storedData = data;
    }

    public String getMeterId() {
        return meterId;
    }

    public String getRawReading() {
        return rawReading;
    }
}

