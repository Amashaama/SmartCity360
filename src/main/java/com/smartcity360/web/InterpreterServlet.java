package com.smartcity360.web;

import com.smartcity360.dao.PolicyRuleDao;
import com.smartcity360.interpreter.Expression;
import com.smartcity360.interpreter.RuleParser;
import com.smartcity360.interpreter.RuleSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

@WebServlet(name = "InterpreterServlet", urlPatterns = {"/interpreter"})
public class InterpreterServlet extends HttpServlet {

    private static final String MSG_INVALID_RULE =
            "Invalid rule format. Please ensure all conditions are combined with AND or OR.";
    private static final String MSG_MISSING_FIELDS =
            "Please provide both a rule title and a rule expression before saving.";
    private static final String MSG_GENERIC =
            "Something went wrong while processing your request. Please try again.";
    private static final String MSG_PAGE_UNAVAILABLE =
            "The policy rules page is temporarily unavailable. Please try again shortly.";

    public static class EvaluationResult {
        private String ruleName;
        private String ruleText;
        private boolean violationTriggered;
        private String explanation;

        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getRuleText() { return ruleText; }
        public void setRuleText(String ruleText) { this.ruleText = ruleText; }
        public boolean isViolationTriggered() { return violationTriggered; }
        public void setViolationTriggered(boolean violationTriggered) { this.violationTriggered = violationTriggered; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            renderPage(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_PAGE_UNAVAILABLE);
        }
    }

    /** Loads the page data and forwards to the JSP (used by GET and by POST error re-rendering). */
    private void renderPage(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PolicyRuleDao ruleDao = (PolicyRuleDao) getServletContext().getAttribute("policyRuleDao");
        List<PolicyRuleDao.RuleEntity> rules = ruleDao.findAll();
        Set<String> keywords = ruleDao.getAllKeywords();

        List<String> preferredOrder = List.of("HIGH_NOISE", "RESTRICTED_HOURS", "HAZARDOUS_SMOKE", "ILLEGAL_PARKING", "EMERGENCY_ZONE");
        List<String> sortedKeywords = new ArrayList<>(keywords);
        sortedKeywords.sort(Comparator.comparingInt(k -> {
            int idx = preferredOrder.indexOf(k);
            return idx != -1 ? idx : Integer.MAX_VALUE;
        }));

        req.setAttribute("rules", rules);
        req.setAttribute("keywords", sortedKeywords);
        req.getRequestDispatcher("/WEB-INF/views/interpreter.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            PolicyRuleDao ruleDao = (PolicyRuleDao) getServletContext().getAttribute("policyRuleDao");
            String action = req.getParameter("action");

            if ("addRule".equals(action)) {
                String ruleName = req.getParameter("ruleName");
                String ruleText = req.getParameter("ruleText");
                String description = req.getParameter("description");

                if (ruleName == null || ruleName.isBlank() || ruleText == null || ruleText.isBlank()) {
                    showError(req, resp, MSG_MISSING_FIELDS);
                    return;
                }

                try {
                    // Parse rule to validate syntax and verify tokens against allowed keywords
                    Set<String> keywords = ruleDao.getAllKeywords();
                    RuleParser.parse(ruleText, keywords);
                } catch (RuleSyntaxException ex) {
                    ex.printStackTrace();
                    showError(req, resp, MSG_INVALID_RULE);
                    return;
                }

                String safeDescription = description == null ? "" : description.trim();
                ruleDao.saveRule(ruleName.trim(), ruleText.trim(), safeDescription);
                req.getSession().setAttribute("flashMessage", "Municipal policy rule '" + ruleName.trim() + "' successfully configured and active.");
                req.getSession().setAttribute("flashType", "success");
            } else if ("deleteRule".equals(action)) {
                long id = Long.parseLong(req.getParameter("id"));
                ruleDao.deleteRule(id);
                req.getSession().setAttribute("flashMessage", "Municipal policy rule deleted.");
                req.getSession().setAttribute("flashType", "info");
            } else if ("toggleRule".equals(action)) {
                long id = Long.parseLong(req.getParameter("id"));
                boolean active = Boolean.parseBoolean(req.getParameter("active"));
                ruleDao.toggleActive(id, !active);
                req.getSession().setAttribute("flashMessage", "Municipal policy rule " + (!active ? "activated" : "deactivated") + ".");
                req.getSession().setAttribute("flashType", "info");
            } else if ("evaluateConditions".equals(action)) {
                String[] selectedKeywords = req.getParameterValues("activeKeywords");
                String sensorContext = selectedKeywords != null ? String.join(",", selectedKeywords) : "";

                List<PolicyRuleDao.RuleEntity> rules = ruleDao.findAll();
                List<EvaluationResult> evalResults = new ArrayList<>();
                Set<String> keywords = ruleDao.getAllKeywords();

                for (PolicyRuleDao.RuleEntity r : rules) {
                    if (!r.isActive()) continue;
                    try {
                        Expression expr = RuleParser.parse(r.getRuleText(), keywords);
                        boolean match = expr.interpret(sensorContext);
                        EvaluationResult res = new EvaluationResult();
                        res.setRuleName(r.getRuleName());
                        res.setRuleText(r.getRuleText());
                        res.setViolationTriggered(match);
                        res.setExplanation(match ? "Policy condition triggered by active municipal sensor telemetry." :
                                "Compliant: Current zone telemetry satisfies policy criteria.");
                        evalResults.add(res);
                    } catch (Exception ex) {
                       
                        ex.printStackTrace();
                    }
                }
                req.getSession().setAttribute("evalResults", evalResults);
                req.getSession().setAttribute("sensorContext", sensorContext);
                resp.sendRedirect(req.getContextPath() + "/interpreter#evaluator");
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/interpreter");
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
