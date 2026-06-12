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
import java.util.HashMap;
import java.util.Map;

public class BuyItemsServlet extends HttpServlet {

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

        String itemIdParam = request.getParameter("itemId");
        if (itemIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"itemId parameter is required.\"}");
            return;
        }

        int itemId;
        try {
            itemId = Integer.parseInt(itemIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"itemId must be a number.\"}");
            return;
        }

        String sql = "SELECT item_id, name, color, description, price, available FROM items WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("itemId",      rs.getInt("item_id"));
                    item.put("name",        rs.getString("name"));
                    item.put("color",       rs.getString("color"));
                    item.put("description", rs.getString("description"));
                    item.put("price",       rs.getDouble("price"));
                    item.put("available",   rs.getBoolean("available"));
                    out.print(mapper.writeValueAsString(item));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Item not found.\"}");
                }
            }

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
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

        String itemIdParam = request.getParameter("itemId");
        if (itemIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"itemId parameter is required.\"}");
            return;
        }

        int itemId;
        int userId = (int) session.getAttribute("userId");

        try {
            itemId = Integer.parseInt(itemIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"itemId must be a number.\"}");
            return;
        }

        String checkSql  = "SELECT cart_id, quantity FROM cart WHERE user_id = ? AND item_id = ?";
        String updateSql = "UPDATE cart SET quantity = quantity + 1 WHERE cart_id = ?";
        String insertSql = "INSERT INTO cart (user_id, item_id, quantity) VALUES (?, ?, 1)";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setInt(1, userId);
                check.setInt(2, itemId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        int cartId = rs.getInt("cart_id");
                        try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                            upd.setInt(1, cartId);
                            upd.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                            ins.setInt(1, userId);
                            ins.setInt(2, itemId);
                            ins.executeUpdate();
                        }
                    }
                }
            }

            out.print("{\"success\":true,\"message\":\"Item added to cart.\"}");

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }
}
