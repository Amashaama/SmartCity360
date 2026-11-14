package com.smartcity360.web;

import com.smartcity360.dao.MeterTransmissionDao;
import com.smartcity360.decorator.AuthenticationDecorator;
import com.smartcity360.decorator.StreamDemo;
import com.smartcity360.decorator.StreamOptions;
import com.smartcity360.decorator.StreamTrace;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "DecoratorServlet", urlPatterns = {"/decorator"})
public class DecoratorServlet extends HttpServlet {

    private static final String MSG_GENERIC =
            "Transmission failed. Please check the meter details and try again.";
    private static final String MSG_PAGE_UNAVAILABLE =
            "The meter telemetry page is temporarily unavailable. Please try again shortly.";

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
        MeterTransmissionDao meterDao = (MeterTransmissionDao) getServletContext().getAttribute("meterTransmissionDao");
        List<MeterTransmissionDao.TransmissionRecord> history = meterDao.findRecent(500);

        // Provide a default trace on first page load so the pipeline table is populated immediately
        if (req.getSession().getAttribute("trace") == null) {
            String defaultMeter = "METER-101";
            String defaultReading = "Meter 101:    420.5 kWh    at   10:30   [Normal Load]";
            String autoToken = AuthenticationDecorator.generateDeviceToken(defaultMeter);
            StreamOptions defaultOpts = new StreamOptions(true, true, true, autoToken);
            StreamTrace defaultTrace = StreamDemo.send(defaultMeter, defaultReading, defaultOpts);

            req.getSession().setAttribute("trace", defaultTrace);
            req.getSession().setAttribute("activeToken", autoToken);
            req.getSession().setAttribute("activeMeterId", defaultMeter);
            req.getSession().setAttribute("activeReading", defaultReading);
        }

        req.setAttribute("history", history);
        req.getRequestDispatcher("/WEB-INF/views/decorator.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            MeterTransmissionDao meterDao = (MeterTransmissionDao) getServletContext().getAttribute("meterTransmissionDao");

            String action = req.getParameter("action");
            String meterId = req.getParameter("meterId");
            String reading = req.getParameter("reading");

            boolean compress = "on".equalsIgnoreCase(req.getParameter("compress"));
            boolean encrypt = "on".equalsIgnoreCase(req.getParameter("encrypt"));
            boolean authenticate = "on".equalsIgnoreCase(req.getParameter("authenticate"));

            if (meterId == null || meterId.isBlank()) meterId = "METER-101";
            if (reading == null || reading.isBlank()) {
                reading = "Meter 101:    420.5 kWh    at   10:30   [Normal Load]";
            }

            // Automatically generate HMAC-SHA256 device token for the selected meter (no manual key typing)
            String token = AuthenticationDecorator.generateDeviceToken(meterId);

            StreamOptions options = new StreamOptions(compress, encrypt, authenticate, token);

            if ("verifyToken".equals(action) || "testWrongToken".equals(action)) {
                String candidateToken = "TAMPERED".equalsIgnoreCase(req.getParameter("mode"))
                        ? "INVALID-KEY-999"
                        : token;

                StreamDemo.VerificationResult result = StreamDemo.verifyToken(meterId, reading, options, candidateToken);
                req.getSession().setAttribute("verificationResult", result);
                req.getSession().setAttribute("testedToken", candidateToken);

                if (!result.isSuccess()) {
                    req.getSession().setAttribute("wrongTokenMessage",
                            "Access Denied: " + result.getErrorMessage() + ". Telemetry stream read blocked.");
                } else {
                    req.getSession().removeAttribute("wrongTokenMessage");
                }

                resp.sendRedirect(req.getContextPath() + "/decorator#tamperTest");
                return;
            }

            // Standard transmission
            StreamTrace trace = StreamDemo.send(meterId, reading, options);

            // Log to MySQL database
            meterDao.logTransmission(meterId, reading, compress, encrypt, authenticate,
                    trace.getFinalSent(), trace.getReadBack());

            req.getSession().removeAttribute("verificationResult");
            req.getSession().removeAttribute("wrongTokenMessage");
            req.getSession().setAttribute("trace", trace);
            req.getSession().setAttribute("activeToken", token);
            req.getSession().setAttribute("activeMeterId", meterId);
            req.getSession().setAttribute("activeReading", reading);
            req.getSession().setAttribute("flashMessage", "Meter telemetry reading transmitted and logged successfully.");
            req.getSession().setAttribute("flashType", "success");

            resp.sendRedirect(req.getContextPath() + "/decorator#pipelineResult");
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
