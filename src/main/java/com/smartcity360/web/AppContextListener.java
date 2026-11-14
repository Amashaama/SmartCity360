package com.smartcity360.web;

import com.smartcity360.chain.ChainBuilder;
import com.smartcity360.chain.ComplaintHandler;
import com.smartcity360.dao.*;
import com.smartcity360.persistence.DatabaseManager;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(AppContextListener.class);

    public static final String CITY_NAME = "SmartCity 360";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Starting SmartCity360 web application...");
        ServletContext ctx = sce.getServletContext();

       
        DatabaseManager.initialize();

        ZoneDao zoneDao = new ZoneDao();
        ComplaintDao complaintDao = new ComplaintDao();
        StreetLightDao streetLightDao = new StreetLightDao();
        PolicyRuleDao policyRuleDao = new PolicyRuleDao();
        MeterTransmissionDao meterTransmissionDao = new MeterTransmissionDao();
        UserDao userDao = new UserDao();

        zoneDao.seedInitialCityIfEmpty(CITY_NAME);
        complaintDao.seedInitialComplaintsIfEmpty();
        streetLightDao.seedInitialLightsIfEmpty();
        policyRuleDao.seedInitialRulesIfEmpty();
        meterTransmissionDao.seedInitialTransmissionsIfEmpty();
        userDao.seedInitialUsersIfEmpty();

      
        ComplaintHandler complaintChain = ChainBuilder.buildChain();

      
        ctx.setAttribute("zoneDao", zoneDao);
        ctx.setAttribute("complaintDao", complaintDao);
        ctx.setAttribute("streetLightDao", streetLightDao);
        ctx.setAttribute("policyRuleDao", policyRuleDao);
        ctx.setAttribute("meterTransmissionDao", meterTransmissionDao);
        ctx.setAttribute("userDao", userDao);
        ctx.setAttribute("complaintChain", complaintChain);
        ctx.setAttribute("databaseType", DatabaseManager.getActiveDatabaseType());
        ctx.setAttribute("isFallbackInMemory", DatabaseManager.isFallbackInMemory());

        log.info("SmartCity360 initialized successfully. Ready to serve requests.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Shutting down SmartCity360 application...");
        DatabaseManager.shutdown();
    }
}

