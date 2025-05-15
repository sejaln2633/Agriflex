import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;

@WebServlet("/manage-equipment")
public class ManageEquipmentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriflexdb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            // ✅ Handle Deletion if deleteId is passed
            String deleteId = request.getParameter("deleteId");
            if (deleteId != null && !deleteId.isEmpty()) {
                String deleteQuery = "DELETE FROM equipment WHERE equipment_id = ?";
                PreparedStatement pstDelete = conn.prepareStatement(deleteQuery);
                pstDelete.setInt(1, Integer.parseInt(deleteId));
                pstDelete.executeUpdate();

                // ✅ Redirect with message flag
                response.sendRedirect("manage-equipment?deleted=true");
                return;
            }

            // ✅ Check for deleted=true to trigger popup
            String deletedFlag = request.getParameter("deleted");

            int availableEquipment = 0;
            int rentedEquipment = 0;

            String availableQuery = "SELECT COUNT(*) FROM equipment WHERE equipment_id NOT IN (SELECT equipment_id FROM rentals)";
            PreparedStatement pstAvailable = conn.prepareStatement(availableQuery);
            ResultSet rsAvailable = pstAvailable.executeQuery();
            availableEquipment = rsAvailable.next() ? rsAvailable.getInt(1) : 0;

            String rentedQuery = "SELECT COUNT(*) FROM rentals";
            PreparedStatement pstRented = conn.prepareStatement(rentedQuery);
            ResultSet rsRented = pstRented.executeQuery();
            rentedEquipment = rsRented.next() ? rsRented.getInt(1) : 0;

            // ✅ Fixed Total Equipment Count
            int totalEquipment = availableEquipment + rentedEquipment;

            // Fetch available equipment
            String queryAvailableDetails = "SELECT e.equipment_id, e.name, e.description, e.rental_price, e.availability_status, e.created_at, u.name AS farmer_name FROM equipment e INNER JOIN users u ON e.farmer_id = u.id WHERE e.equipment_id NOT IN (SELECT equipment_id FROM rentals)";
            PreparedStatement pstAvailableDetails = conn.prepareStatement(queryAvailableDetails);
            ResultSet rsAvailableDetails = pstAvailableDetails.executeQuery();

            // Fetch rented equipment
            String queryRentedDetails = "SELECT r.rental_id, r.equipment_id, e.name AS equipment_name, e.farmer_id, u1.name AS farmer_name, r.customer_id, u2.name AS customer_name, r.start_date, r.end_date, r.status FROM rentals r INNER JOIN equipment e ON r.equipment_id = e.equipment_id INNER JOIN users u1 ON e.farmer_id = u1.id INNER JOIN users u2 ON r.customer_id = u2.id";
            PreparedStatement pstRentedDetails = conn.prepareStatement(queryRentedDetails);
            ResultSet rsRentedDetails = pstRentedDetails.executeQuery();

            // Date formatter to remove time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            // Start HTML Output
            out.println("<html><head><title>Manage Equipment</title>");

            // ✅ Add alert popup if deleted=true
            if ("true".equals(deletedFlag)) {
                out.println("<script>alert('Equipment deleted successfully!');</script>");
            }

            out.println("</head><body>");
            out.println("<h2>Equipment Summary</h2>");
            out.println("<p><strong>Total Equipment:</strong> " + totalEquipment + "</p>");
            out.println("<p><strong>Available Equipment:</strong> " + availableEquipment + "</p>");
            out.println("<p><strong>Rented Equipment:</strong> " + rentedEquipment + "</p>");

            // ✅ Available Equipment Table with Delete Option
            out.println("<h3>Available Equipment</h3>");
            out.println("<table border='1'><tr><th>ID</th><th>Name</th><th>Description</th><th>Rental Price</th><th>Status</th><th>Created At</th><th>Farmer Name</th><th>Action</th></tr>");
            while (rsAvailableDetails.next()) {
                int equipmentId = rsAvailableDetails.getInt("equipment_id");
                Timestamp createdAt = rsAvailableDetails.getTimestamp("created_at");
                String formattedCreatedAt = (createdAt != null) ? sdf.format(createdAt) : "";

                out.println("<tr><td>" + equipmentId + "</td>");
                out.println("<td>" + rsAvailableDetails.getString("name") + "</td>");
                out.println("<td>" + rsAvailableDetails.getString("description") + "</td>");
                out.println("<td>" + rsAvailableDetails.getDouble("rental_price") + "</td>");
                out.println("<td>" + rsAvailableDetails.getString("availability_status") + "</td>");
                out.println("<td>" + formattedCreatedAt + "</td>");
                out.println("<td>" + rsAvailableDetails.getString("farmer_name") + "</td>");
                out.println("<td><a href='manage-equipment?deleteId=" + equipmentId + "' onclick='return confirm(\"Are you sure you want to delete this equipment?\")'>Delete</a></td></tr>");
            }
            out.println("</table>");

            // Rented Equipment Table
            out.println("<h3>Rented Equipment</h3>");
            out.println("<table border='1'><tr><th>Rental ID</th><th>Equipment ID</th><th>Equipment Name</th><th>Farmer ID</th><th>Farmer Name</th><th>Customer ID</th><th>Customer Name</th><th>Start Date</th><th>End Date</th><th>Status</th></tr>");
            while (rsRentedDetails.next()) {
                Timestamp startDate = rsRentedDetails.getTimestamp("start_date");
                Timestamp endDate = rsRentedDetails.getTimestamp("end_date");
                String formattedStartDate = (startDate != null) ? sdf.format(startDate) : "";
                String formattedEndDate = (endDate != null) ? sdf.format(endDate) : "";

                out.println("<tr><td>" + rsRentedDetails.getInt("rental_id") + "</td>");
                out.println("<td>" + rsRentedDetails.getInt("equipment_id") + "</td>");
                out.println("<td>" + rsRentedDetails.getString("equipment_name") + "</td>");
                out.println("<td>" + rsRentedDetails.getInt("farmer_id") + "</td>");
                out.println("<td>" + rsRentedDetails.getString("farmer_name") + "</td>");
                out.println("<td>" + rsRentedDetails.getInt("customer_id") + "</td>");
                out.println("<td>" + rsRentedDetails.getString("customer_name") + "</td>");
                out.println("<td>" + formattedStartDate + "</td>");
                out.println("<td>" + formattedEndDate + "</td>");
                out.println("<td>" + rsRentedDetails.getString("status") + "</td></tr>");
            }
            out.println("</table>");

            out.println("</body></html>");
            conn.close();
        } catch (Exception e) {
            out.println("<p>Error: " + e.getMessage() + "</p>");
        }
    }
}
