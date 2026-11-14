package com.smartcity360.web;

import com.smartcity360.chain.Complaint;
import com.smartcity360.chain.ComplaintHandler;
import com.smartcity360.dao.ComplaintDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "ChainServlet", urlPatterns = {"/chain"})
public class ChainServlet extends HttpServlet {

    private static final String MSG_MISSING_FIELDS =
            "Please fill in the complaint title, resident name and location before submitting.";
    private static final String MSG_GENERIC =
            "We could not submit the complaint. Please check the details and try again.";
    private static final String MSG_PAGE_UNAVAILABLE =
            "The complaints page is temporarily unavailable. Please try again shortly.";

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
        ComplaintDao complaintDao = (ComplaintDao) getServletContext().getAttribute("complaintDao");
        List<ComplaintDao.ComplaintRecord> complaints = complaintDao.findAll();

        req.setAttribute("complaints", complaints);
        req.getRequestDispatcher("/WEB-INF/views/chain.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ComplaintDao complaintDao = (ComplaintDao) getServletContext().getAttribute("complaintDao");
            ComplaintHandler chain = (ComplaintHandler) getServletContext().getAttribute("complaintChain");

            String title = req.getParameter("title");
            String residentName = req.getParameter("residentName");
            String description = req.getParameter("description");
            String location = req.getParameter("location");

            if (isBlank(title) || isBlank(residentName) || isBlank(location)) {
                showError(req, resp, MSG_MISSING_FIELDS);
                return;
            }

            int severity = Integer.parseInt(req.getParameter("severity"));

            // Create complaint request
            Complaint complaint = new Complaint(title, residentName, severity);

            // Process request through escalation handler pipeline
            chain.handleComplaint(complaint);

         
            long savedId = complaintDao.saveComplaint(complaint, description, location);

            req.getSession().setAttribute("flashMessage",
                    "Complaint ticket #" + savedId + " routed to " + complaint.getHandledBy() +
                    " (" + complaint.getStatus() + ").");
            req.getSession().setAttribute("flashType", "success");
            req.getSession().setAttribute("lastProcessedId", savedId);

            resp.sendRedirect(req.getContextPath() + "/chain");
        } catch (Exception e) {
            e.printStackTrace();
            showError(req, resp, MSG_GENERIC);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
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
