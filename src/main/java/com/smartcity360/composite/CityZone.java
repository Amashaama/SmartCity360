package com.smartcity360.composite;


public interface CityZone {

 
    void shutdownPower();

 
    void restorePower();

   
    String reportUsage();

    /** Returns the total power usage (in kWh) of this zone and everything inside it. */
    int getPowerUsage();
}

