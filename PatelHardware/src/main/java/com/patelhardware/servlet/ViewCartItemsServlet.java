package com.patelhardware.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewCartItemsServlet extends HttpServlet {

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

        int userId = (int) session.getAttribute("userId");

        String sql = "SELECT i.item_id, i.name, i.color, i.description, i.price, "
                   + "i.available, c.quantity "
                   + "FROM cart c "
                   + "JOIN items i ON c.item_id = i.item_id "
                   + "WHERE c.user_id = ?";

        List<Map<String, Object>> cartItems = new ArrayList<>();
        double total = 0.0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double price    = rs.getDouble("price");
                    int    quantity = rs.getInt("quantity");

                    Map<String, Object> row = new HashMap<>();
                    row.put("itemId",      rs.getInt("item_id"));
                    row.put("name",        rs.getString("name"));
                    row.put("color",       rs.getString("color"));
                    row.put("description", rs.getString("description"));
                    row.put("price",       price);
                    row.put("available",   rs.getBoolean("available"));
                    row.put("quantity",    quantity);

                    cartItems.add(row);
                    total += price * quantity;
                }
            }

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("items", cartItems);
            responseBody.put("total", total);

            out.print(mapper.writeValueAsString(responseBody));

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }
}
