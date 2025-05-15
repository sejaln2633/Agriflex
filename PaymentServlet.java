

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
import jakarta.servlet.RequestDispatcher;

@WebServlet("/PaymentServlet")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rentalId = request.getParameter("rental_id");

        if (rentalId == null || rentalId.isEmpty()) {
            response.getWriter().println("Error: Missing required parameters.");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/your_database", "root", "password");

            String query = "SELECT r.customer_id, e.equipment_id, e.name AS equipment_name, u.name AS farmer_name, e.rental_price " +
                           "FROM rentals r " +
                           "JOIN equipment e ON r.equipment_id = e.equipment_id " +
                           "JOIN users u ON e.farmer_id = u.id " +
                           "WHERE r.rental_id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, Integer.parseInt(rentalId));
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                request.setAttribute("rentalId", rentalId);
                request.setAttribute("equipmentName", rs.getString("equipment_name"));
                request.setAttribute("farmerName", rs.getString("farmer_name"));
                request.setAttribute("amount", rs.getDouble("rental_price"));

                RequestDispatcher dispatcher = request.getRequestDispatcher("payment.html");
                dispatcher.forward(request, response);
            } else {
                response.getWriter().println("Error: Invalid rental request.");
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error: " + e.getMessage());
        }
    }
}
