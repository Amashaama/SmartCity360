package com.smartcity360.flyweight;

import java.util.HashMap;
import java.util.Map;


public final class StreetLightFactory {

    private static final String KEY_SEPARATOR = "|";

    //key = typeName|bulbModel|wattage, value = the one shared type object.
    private static final Map<String, StreetLightType> typePool = new HashMap<>();

    private StreetLightFactory() {
      
    }

 
    public static StreetLightType getStreetLightType(String typeName, String bulbModel, int wattage) {
        String key = typeName + KEY_SEPARATOR + bulbModel + KEY_SEPARATOR + wattage;
        synchronized (typePool) {
            StreetLightType type = typePool.get(key);
            if (type == null) {
                type = new StreetLightType(typeName, bulbModel, wattage);
                typePool.put(key, type);
            }
            return type;
        }
    }

 
    public static int getPoolSize() {
        synchronized (typePool) {
            return typePool.size();
        }
    }

   
    public static void clearPool() {
        synchronized (typePool) {
            typePool.clear();
        }
    }
}

