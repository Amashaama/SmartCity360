package com.smartcity360.web;

import com.smartcity360.dao.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/logout"})
public class AuthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if ("/logout".equals(servletPath)) {
            performLogout(req, resp);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("loggedInUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if ("/logout".equals(servletPath)) {
            performLogout(req, resp);
            return;
        }

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        UserDao userDao = (UserDao) getServletContext().getAttribute("userDao");
        if (userDao == null) {
            userDao = new UserDao();
        }

        UserDao.UserRecord user = userDao.authenticate(username, password);
        if (user != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("loggedInUser", user);
            session.setAttribute("flashMessage", "Welcome back, " + user.getFullName() + "!");
            session.setAttribute("flashType", "success");
            resp.sendRedirect(req.getContextPath() + "/");
        } else {
            req.setAttribute("errorMessage", "Invalid username or password. Please verify your credentials and try again.");
            req.setAttribute("enteredUsername", username);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }

    private void performLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login?logout=1");
    }
}

