import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;



@WebServlet("/ViewMyEquipmentsServlet")
public class ViewMyEquipmentsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriflexdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        displayEquipments(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String deleteId = request.getParameter("deleteId");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        int farmerId = (int) session.getAttribute("userId");

        if (deleteId != null && !deleteId.trim().isEmpty()) {
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String deleteQuery = "DELETE FROM equipment WHERE equipment_id = ? AND farmer_id = ?";
                PreparedStatement pst = con.prepareStatement(deleteQuery);
                pst.setInt(1, Integer.parseInt(deleteId));
                pst.setInt(2, farmerId);
                pst.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        displayEquipments(request, response);
    }

    private void displayEquipments(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            out.println("<tr><td colspan='7'>Please log in first.</td></tr>");
            return;
        }

        int farmerId = (int) session.getAttribute("userId");

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT equipment_id, name, description, rental_price, availability_status, created_at FROM equipment WHERE farmer_id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, farmerId);
            ResultSet rs = pst.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int id = rs.getInt("equipment_id");

                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + rs.getString("name") + "</td>");
                out.println("<td>" + rs.getString("description") + "</td>");
                out.println("<td>" + rs.getDouble("rental_price") + "</td>");
                out.println("<td>" + rs.getString("availability_status") + "</td>");
                out.println("<td>" + rs.getTimestamp("created_at") + "</td>");
                out.println("<td><button onclick='deleteEquipment(" + id + ")' style='background-color:#d63031;color:white;border:none;padding:6px 12px;border-radius:5px;cursor:pointer;'>Delete</button></td>");
                out.println("</tr>");
            }

            if (!hasData) {
                out.println("<tr><td colspan='7'>No equipment found.</td></tr>");
            }
        } catch (Exception e) {
            out.println("<tr><td colspan='7'>Error: " + e.getMessage() + "</td></tr>");
            e.printStackTrace();
        }
    }
}