package com.patelhardware.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patelhardware.model.RegistrationResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistrationServlet extends HttpServlet {

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
                    new RegistrationResponse(false, "Username and password are required.")));
            return;
        }

        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'user')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();

            out.print(mapper.writeValueAsString(
                    new RegistrationResponse(true, "Registration successful. Please log in.")));

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print(mapper.writeValueAsString(
                        new RegistrationResponse(false, "Username already exists.")));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(mapper.writeValueAsString(
                        new RegistrationResponse(false, "Database error: " + e.getMessage())));
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/html/register.html");
    }
}
