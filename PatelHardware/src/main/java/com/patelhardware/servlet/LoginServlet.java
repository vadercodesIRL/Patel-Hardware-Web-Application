package com.patelhardware.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patelhardware.model.LoginResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        PrintWriter out = response.getWriter();

        if (username == null || username.isBlank() ||
            password == null || password.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(mapper.writeValueAsString(
                    new LoginResponse(false, "Username and password are required.", null, null)));
            return;
        }

        String sql = "SELECT user_id, username, role FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");

                    HttpSession session = request.getSession(true);
                    session.setAttribute("username", username);
                    session.setAttribute("role",     role);
                    session.setAttribute("userId",   rs.getInt("user_id"));

                    LoginResponse lr = new LoginResponse(true, "Login successful.", username, role);
                    out.print(mapper.writeValueAsString(lr));
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.print(mapper.writeValueAsString(
                            new LoginResponse(false, "Invalid username or password.", null, null)));
                }
            }

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(mapper.writeValueAsString(
                    new LoginResponse(false, "Database error: " + e.getMessage(), null, null)));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/html/login.html");
    }
}
