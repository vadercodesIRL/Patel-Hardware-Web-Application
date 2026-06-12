package com.patelhardware.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patelhardware.model.Items;

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
import java.util.ArrayList;
import java.util.List;

public class SearchItemsServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        PrintWriter out = response.getWriter();

        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Not authenticated.\"}");
            return;
        }

        String query = request.getParameter("query");
        if (query == null || query.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"query parameter is required.\"}");
            return;
        }

        String likeParam = "%" + query.trim() + "%";
        String sql = "SELECT item_id, name, color, description, price, available "
                   + "FROM items "
                   + "WHERE name LIKE ? OR description LIKE ?";

        List<Items> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, likeParam);
            ps.setString(2, likeParam);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Items item = new Items(
                            rs.getInt("item_id"),
                            rs.getString("name"),
                            rs.getString("color"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getBoolean("available")
                    );
                    results.add(item);
                }
            }

            out.print(mapper.writeValueAsString(results));

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }
}
