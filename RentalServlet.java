

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/RentalServlet")
public class RentalServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriflexdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("rent".equals(action)) {
            String customerId = request.getParameter("customer_id");
            String equipmentId = request.getParameter("equipment_id");
            String startDate = request.getParameter("start_date");
            String endDate = request.getParameter("end_date");

            if (customerId == null || equipmentId == null || startDate == null || endDate == null ||
                customerId.isEmpty() || equipmentId.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                response.getWriter().write("All fields are required!");
                return;
            }

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                String checkQuery = "SELECT status, farmer_id FROM equipment WHERE id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, Integer.parseInt(equipmentId));
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    String status = rs.getString("status");
                    int farmerId = rs.getInt("farmer_id");

                    if (!"available".equalsIgnoreCase(status)) {
                        response.getWriter().write("Equipment is not available.");
                        return;
                    }

                    String insertQuery = "INSERT INTO rentals (customer_id, farmer_id, equipment_id, start_date, end_date, status) VALUES (?, ?, ?, ?, ?, 'pending')";
                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                    insertStmt.setInt(1, Integer.parseInt(customerId));
                    insertStmt.setInt(2, farmerId);
                    insertStmt.setInt(3, Integer.parseInt(equipmentId));
                    insertStmt.setString(4, startDate);
                    insertStmt.setString(5, endDate);

                    insertStmt.executeUpdate();
                    response.getWriter().write("Rental request submitted!");

                    insertStmt.close();
                }

                rs.close();
                checkStmt.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
                response.getWriter().write("Database error: " + e.getMessage());
            }
        }
    }
}
