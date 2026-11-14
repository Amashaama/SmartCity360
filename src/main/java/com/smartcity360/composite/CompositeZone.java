package com.smartcity360.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CompositeZone implements CityZone {

    private static final String LINE_BREAK = "\n";
    private static final String INDENT = "    ";

    private long id;               
    private String zoneName;
    private final List<CityZone> children = new ArrayList<>();

    public CompositeZone(String zoneName) {
        this.zoneName = zoneName;
    }

    public void addZone(CityZone zone) {
        children.add(zone);
    }

    public void removeZone(CityZone zone) {
        children.remove(zone);
    }

  
    @Override
    public void shutdownPower() {
        for (CityZone child : children) {
            child.shutdownPower();
        }
    }


    @Override
    public void restorePower() {
        for (CityZone child : children) {
            child.restorePower();
        }
    }

  
    @Override
    public String reportUsage() {
        StringBuilder report = new StringBuilder();
        report.append(zoneName).append(": total ").append(getPowerUsage()).append(" kWh");
        for (CityZone child : children) {
            for (String line : child.reportUsage().split(LINE_BREAK)) {
                report.append(LINE_BREAK).append(INDENT).append(line);
            }
        }
        return report.toString();
    }

 
    @Override
    public int getPowerUsage() {
        int total = 0;
        for (CityZone child : children) {
            total += child.getPowerUsage();
        }
        return total;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

   
    public List<CityZone> getChildren() {
        return Collections.unmodifiableList(children);
    }

  
    public CompositeZone findZone(String name) {
        if (zoneName.equals(name)) {
            return this;
        }
        for (CityZone child : children) {
            if (child instanceof CompositeZone) {
                CompositeZone found = ((CompositeZone) child).findZone(name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

   
    public List<String> getAllZoneNames() {
        List<String> names = new ArrayList<>();
        names.add(zoneName);
        for (CityZone child : children) {
            if (child instanceof CompositeZone) {
                names.addAll(((CompositeZone) child).getAllZoneNames());
            }
        }
        return names;
    }
}

