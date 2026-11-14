package com.smartcity360.web;

import com.smartcity360.dao.StreetLightDao;
import com.smartcity360.flyweight.FlyweightDemo;
import com.smartcity360.flyweight.FlyweightResult;
import com.smartcity360.flyweight.MemoryReport;
import com.smartcity360.flyweight.StreetLightFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "FlyweightServlet", urlPatterns = {"/flyweight"})
public class FlyweightServlet extends HttpServlet {

    private static final String MSG_MISSING_FIELDS =
            "Please provide both a light code and GPS coordinates before registering.";
    private static final String MSG_GENERIC =
            "We could not complete that street light request. Please check the details and try again.";
    private static final String MSG_PAGE_UNAVAILABLE =
            "The street light page is temporarily unavailable. Please try again shortly.";

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
        StreetLightDao lightDao = (StreetLightDao) getServletContext().getAttribute("streetLightDao");

        List<StreetLightDao.LightRecord> sampleLights = lightDao.findRecentLights(500);
        int totalLights = lightDao.countTotalLights();
        int poolSize = lightDao.countDistinctTypes();

        MemoryReport memoryReport = new MemoryReport(totalLights > 0 ? totalLights : 50, poolSize > 0 ? poolSize : 5);

        req.setAttribute("sampleLights", sampleLights);
        req.setAttribute("totalLights", totalLights);
        req.setAttribute("poolSize", poolSize);
        req.setAttribute("memoryReport", memoryReport);

        req.getRequestDispatcher("/WEB-INF/views/flyweight.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            StreetLightDao lightDao = (StreetLightDao) getServletContext().getAttribute("streetLightDao");
            String action = req.getParameter("action");

            if ("addSingle".equals(action)) {
                String lightCode = req.getParameter("lightCode");
                String gps = req.getParameter("gps");
                String typeName = req.getParameter("typeName");
                String bulbModel = req.getParameter("bulbModel");

                if (lightCode == null || lightCode.isBlank() || gps == null || gps.isBlank()) {
                    showError(req, resp, MSG_MISSING_FIELDS);
                    return;
                }

                int wattage = Integer.parseInt(req.getParameter("wattage"));
                boolean isOn = "on".equalsIgnoreCase(req.getParameter("isOn"));

                // Obtains shared intrinsic flyweight object from factory
                StreetLightFactory.getStreetLightType(typeName, bulbModel, wattage);

                lightDao.addStreetLight(lightCode.trim(), gps.trim(), isOn, typeName, bulbModel, wattage);
                req.getSession().setAttribute("flashMessage", "Street light unit " + lightCode.trim() + " registered to municipal fleet (" + typeName + ").");
                req.getSession().setAttribute("flashType", "success");
            } else if ("toggleLight".equals(action)) {
                long id = Long.parseLong(req.getParameter("id"));
                boolean newState = Boolean.parseBoolean(req.getParameter("newState"));
                lightDao.toggleLight(id, newState);
                req.getSession().setAttribute("flashMessage", "Street light " + id + " powered " + (newState ? "ON" : "OFF") + ".");
                req.getSession().setAttribute("flashType", "info");
            } else if ("runBulkDemo".equals(action)) {
                // Run 50,000 objects in memory benchmark
                FlyweightResult result = FlyweightDemo.run();
                req.getSession().setAttribute("bulkResult", result);
                resp.sendRedirect(req.getContextPath() + "/flyweight#bulkDemo");
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/flyweight");
        } catch (Exception e) {
            e.printStackTrace();
            showError(req, resp, MSG_GENERIC);
        }
    }


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
