package com.smartcity360.web;

import com.smartcity360.composite.Building;
import com.smartcity360.composite.CityBuilder;
import com.smartcity360.composite.CityZone;
import com.smartcity360.composite.CompositeZone;
import com.smartcity360.composite.ScaleDemoResult;
import com.smartcity360.dao.ZoneDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "CompositeServlet", urlPatterns = {"/composite"})
public class CompositeServlet extends HttpServlet {

    private static final String MSG_GENERIC =
            "Zone operation could not be completed. Please check the details and try again.";
    private static final String MSG_MISSING_NAME =
            "Please enter a name before saving.";
    private static final String MSG_PAGE_UNAVAILABLE =
            "The zone grid page is temporarily unavailable. Please try again shortly.";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            renderPage(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_PAGE_UNAVAILABLE);
        }
    }

    private void renderPage(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ZoneDao zoneDao = (ZoneDao) getServletContext().getAttribute("zoneDao");
        CompositeZone city = zoneDao.loadCityHierarchy(CityBuilder.CITY_NAME);
        if (city == null) {
            city = CityBuilder.buildDemoCity();
        }

      
        List<CompositeZone> districts = new ArrayList<>();
        List<CompositeZone> streets = new ArrayList<>();
        for (CityZone child : city.getChildren()) {
            if (child instanceof CompositeZone dZone) {
                districts.add(dZone);
                for (CityZone sChild : dZone.getChildren()) {
                    if (sChild instanceof CompositeZone sZone) {
                        streets.add(sZone);
                    }
                }
            }
        }

        req.setAttribute("city", city);
        req.setAttribute("districts", districts);
        req.setAttribute("streets", streets);
        req.setAttribute("totalPower", city.getPowerUsage());

        req.getRequestDispatcher("/WEB-INF/views/composite.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ZoneDao zoneDao = (ZoneDao) getServletContext().getAttribute("zoneDao");
        String action = req.getParameter("action");
        String message = "";
        String msgType = "success";

        try {
            if ("addDistrict".equals(action)) {
                String districtName = req.getParameter("districtName");
                long cityId = Long.parseLong(req.getParameter("cityId"));
                if (districtName != null && !districtName.isBlank()) {
                    zoneDao.saveZone(cityId, districtName.trim(), "DISTRICT");
                    message = "District '" + districtName.trim() + "' added successfully.";
                } else {
                    showError(req, resp, MSG_MISSING_NAME);
                    return;
                }
            } else if ("addStreet".equals(action)) {
                String streetName = req.getParameter("streetName");
                long districtId = Long.parseLong(req.getParameter("districtId"));
                if (streetName != null && !streetName.isBlank()) {
                    zoneDao.saveZone(districtId, streetName.trim(), "STREET");
                    message = "Street '" + streetName.trim() + "' added to district.";
                } else {
                    showError(req, resp, MSG_MISSING_NAME);
                    return;
                }
            } else if ("addBuilding".equals(action)) {
                String buildingName = req.getParameter("buildingName");
                long streetId = Long.parseLong(req.getParameter("streetId"));
                int powerUsage = Integer.parseInt(req.getParameter("powerUsage"));
                if (buildingName != null && !buildingName.isBlank()) {
                    zoneDao.saveBuilding(streetId, buildingName.trim(), powerUsage, true);
                    message = "Building '" + buildingName.trim() + "' (" + powerUsage + " kWh) added to street.";
                } else {
                    showError(req, resp, MSG_MISSING_NAME);
                    return;
                }
            } else if ("deleteZone".equals(action)) {
                long zoneId = Long.parseLong(req.getParameter("zoneId"));
                zoneDao.deleteZone(zoneId);
                message = "Municipal zone removed from city grid.";
            } else if ("deleteBuilding".equals(action)) {
                long buildingId = Long.parseLong(req.getParameter("buildingId"));
                zoneDao.deleteBuilding(buildingId);
                message = "Facility record removed successfully.";
            } else if ("shutdownZone".equals(action)) {
                long zoneId = Long.parseLong(req.getParameter("zoneId"));
                String zoneName = req.getParameter("zoneName");
                zoneDao.updateAllBuildingsPowerUnderZone(zoneId, false);
                message = "Power supply deactivated for '" + zoneName + "' and connected sub-zones.";
            } else if ("restoreZone".equals(action)) {
                long zoneId = Long.parseLong(req.getParameter("zoneId"));
                String zoneName = req.getParameter("zoneName");
                zoneDao.updateAllBuildingsPowerUnderZone(zoneId, true);
                message = "Power supply restored for '" + zoneName + "' and connected sub-zones.";
            } else if ("toggleBuildingPower".equals(action)) {
                long buildingId = Long.parseLong(req.getParameter("buildingId"));
                boolean currentPower = Boolean.parseBoolean(req.getParameter("currentPower"));
                zoneDao.updateBuildingPowerStatus(buildingId, !currentPower);
                message = "Building power status set to " + (!currentPower ? "ACTIVE" : "STANDBY") + ".";
            } else if ("updateBuilding".equals(action)) {
                long buildingId = Long.parseLong(req.getParameter("buildingId"));
                String name = req.getParameter("name");
                int usage = Integer.parseInt(req.getParameter("powerUsage"));
                boolean powerOn = Boolean.parseBoolean(req.getParameter("powerOn"));
                zoneDao.updateBuilding(buildingId, name, usage, powerOn);
                message = "Building facility details updated.";
            } else if ("runScaleDemo".equals(action)) {
                ScaleDemoResult demoResult = CityBuilder.runScaleDemo();
                req.getSession().setAttribute("scaleResult", demoResult);
                resp.sendRedirect(req.getContextPath() + "/composite#scaleDemo");
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError(req, resp, MSG_GENERIC);
            return;
        }

        req.getSession().setAttribute("flashMessage", message);
        req.getSession().setAttribute("flashType", msgType);
        resp.sendRedirect(req.getContextPath() + "/composite");
    }

    /** Re-renders the page with a friendly, non-technical error banner. */
    private void showError(HttpServletRequest req, HttpServletResponse resp, String message)
            throws ServletException, IOException {
        if (resp.isCommitted()) {
            return;
        }
        req.setAttribute("errorMessage", message);
        try {
            renderPage(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_PAGE_UNAVAILABLE);
            }
        }
    }
}

