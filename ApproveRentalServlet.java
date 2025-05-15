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
import jakarta.servlet.http.HttpSession;

@WebServlet("/ApproveRentalServlet")
public class ApproveRentalServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null || !"farmer".equals(session.getAttribute("role"))) {
            response.getWriter().println("<h3>Error: You must be logged in as a farmer.</h3>");
            return;
        }

        int farmerId = (int) session.getAttribute("userId");
        int rentalId = Integer.parseInt(request.getParameter("rental_id"));
        String action = request.getParameter("action"); // "approve" or "reject"

        String status = action.equals("approve") ? "approved" : "rejected";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");

            // Get equipment_id and ensure it's owned by this farmer
            String getEquipmentIdSQL = "SELECT r.equipment_id FROM rentals r JOIN equipment e ON r.equipment_id = e.equipment_id WHERE r.rental_id = ? AND e.farmer_id = ?";
            PreparedStatement getEqStmt = con.prepareStatement(getEquipmentIdSQL);
            getEqStmt.setInt(1, rentalId);
            getEqStmt.setInt(2, farmerId);
            ResultSet rs = getEqStmt.executeQuery();

            if (rs.next()) {
                int equipmentId = rs.getInt("equipment_id");

                // If approving, set equipment status to rented; else keep it available
                String equipmentStatus = status.equals("approved") ? "rented" : "available";

                String updateEquipmentSQL = "UPDATE equipment SET availability_status=? WHERE equipment_id=?";
                PreparedStatement updateEqStmt = con.prepareStatement(updateEquipmentSQL);
                updateEqStmt.setString(1, equipmentStatus);
                updateEqStmt.setInt(2, equipmentId);
                updateEqStmt.executeUpdate();

                // Update rental status in rentals table (you can remove this if not using farmer_approval anymore)
                String updateRentalSQL = "UPDATE rentals SET status=? WHERE rental_id=?";
                PreparedStatement pst = con.prepareStatement(updateRentalSQL);
                pst.setString(1, status);
                pst.setInt(2, rentalId);

                int rowsUpdated = pst.executeUpdate();

                if (rowsUpdated > 0) {
                    response.sendRedirect("view-rentals.html?success=" + status);
                } else {
                    response.getWriter().println("<h3>Error: Unable to update rental status.</h3>");
                }
            } else {
                response.getWriter().println("<h3>Error: Invalid rental or equipment not owned by farmer.</h3>");
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h3>Error updating rental status.</h3>");
        }
    }
}
