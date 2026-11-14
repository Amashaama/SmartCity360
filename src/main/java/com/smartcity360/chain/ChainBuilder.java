package com.smartcity360.chain;


public final class ChainBuilder {

    private ChainBuilder() {
      
    }

    
    public static ComplaintHandler buildChain() {
        ComplaintHandler localStaffHandler = new LocalStaffHandler();
        ComplaintHandler supervisorHandler = new SupervisorHandler();
        ComplaintHandler cityManagerHandler = new CityManagerHandler();
        ComplaintHandler fallbackHandler = new FallbackHandler();

        localStaffHandler.setNextHandler(supervisorHandler);
        supervisorHandler.setNextHandler(cityManagerHandler);
        cityManagerHandler.setNextHandler(fallbackHandler);

        return localStaffHandler;
    }
}

