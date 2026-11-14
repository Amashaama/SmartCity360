package com.smartcity360.web;

import com.smartcity360.composite.CityBuilder;
import com.smartcity360.composite.CompositeZone;
import com.smartcity360.dao.*;
import com.smartcity360.persistence.DatabaseManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "HomeServlet", urlPatterns = {"", "/home"})
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ZoneDao zoneDao = (ZoneDao) getServletContext().getAttribute("zoneDao");
            ComplaintDao complaintDao = (ComplaintDao) getServletContext().getAttribute("complaintDao");
            StreetLightDao streetLightDao = (StreetLightDao) getServletContext().getAttribute("streetLightDao");
            PolicyRuleDao policyRuleDao = (PolicyRuleDao) getServletContext().getAttribute("policyRuleDao");

            // Load city zone hierarchy for district power load chart
            CompositeZone city = zoneDao != null ? zoneDao.loadCityHierarchy(CityBuilder.CITY_NAME) : null;
            if (city == null || city.getChildren().isEmpty()) {
                city = CityBuilder.buildDemoCity();
            }
            int totalCityPower = city != null ? city.getPowerUsage() : 0;

            // Load complaints & severity breakdown for donut chart
            List<ComplaintDao.ComplaintRecord> complaints = complaintDao != null
                    ? complaintDao.findAll()
                    : Collections.emptyList();
            int totalComplaints = complaints.size();
            int sev1Count = 0;
            int sev2Count = 0;
            int sev3Count = 0;
            int sev4Count = 0;
            int resolvedCount = 0;
            for (ComplaintDao.ComplaintRecord c : complaints) {
                if (c.getSeverity() == 1) sev1Count++;
                else if (c.getSeverity() == 2) sev2Count++;
                else if (c.getSeverity() == 3) sev3Count++;
                else sev4Count++;
                if ("Resolved".equalsIgnoreCase(c.getStatus())) {
                    resolvedCount++;
                }
            }

            // Dashboard stats
            int totalLights = streetLightDao != null ? streetLightDao.countTotalLights() : 0;
            int distinctTypes = streetLightDao != null ? streetLightDao.countDistinctTypes() : 0;
            int totalRules = policyRuleDao != null ? policyRuleDao.findAll().size() : 0;

            req.setAttribute("city", city);
            req.setAttribute("totalCityPower", totalCityPower);
            req.setAttribute("totalComplaints", totalComplaints);
            req.setAttribute("sev1Count", sev1Count);
            req.setAttribute("sev2Count", sev2Count);
            req.setAttribute("sev3Count", sev3Count);
            req.setAttribute("sev4Count", sev4Count);
            req.setAttribute("resolvedCount", resolvedCount);
            req.setAttribute("totalLights", totalLights);
            req.setAttribute("distinctTypes", distinctTypes);
            req.setAttribute("totalRules", totalRules);
            req.setAttribute("dbType", DatabaseManager.getActiveDatabaseType());
            req.setAttribute("isFallbackInMemory", DatabaseManager.isFallbackInMemory());

            req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Unable to load dashboard metrics right now. Please try again.");
            req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
        }
    }
}
