import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/DeletePaymentServlet")
public class DeletePaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String paymentIdStr = request.getParameter("payment_id");

        if (paymentIdStr != null) {
            int paymentId = Integer.parseInt(paymentIdStr);
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");

                String deleteQuery = "DELETE FROM payments WHERE payment_id = ?";
                PreparedStatement pst = con.prepareStatement(deleteQuery);
                pst.setInt(1, paymentId);
                pst.executeUpdate();

                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Redirect back to the payment history page or reload data via AJAX
        response.sendRedirect("viewPaymentHistory.html"); // or wherever your table is displayed
    }
}
