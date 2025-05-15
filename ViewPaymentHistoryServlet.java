import java.io.IOException;
import java.io.PrintWriter;
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

@WebServlet("/ViewPaymentHistoryServlet")
public class ViewPaymentHistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        // Ensure farmer is logged in
        if (session == null || session.getAttribute("userId") == null) {
            out.println("<tr><td colspan='7'>Please log in to view payment history.</td></tr>");
            return;
        }

        int farmerId = (int) session.getAttribute("userId");

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");

            String query = "SELECT p.payment_id, u.name AS customer_name, e.name AS equipment_name, " +
                           "p.amount, p.payment_method, p.payment_status, p.transaction_date " +
                           "FROM payments p " +
                           "JOIN rentals r ON p.rental_id = r.rental_id " +
                           "JOIN users u ON p.customer_id = u.id " +
                           "JOIN equipment e ON r.equipment_id = e.equipment_id " +
                           "WHERE p.farmer_id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, farmerId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int paymentId = rs.getInt("payment_id");

                out.println("<tr>");
                out.println("<td>" + paymentId + "</td>");
                out.println("<td>" + rs.getString("customer_name") + "</td>");
                out.println("<td>" + rs.getString("equipment_name") + "</td>");
                out.println("<td>" + rs.getDouble("amount") + "</td>");
                out.println("<td>" + rs.getString("payment_method") + "</td>");
                out.println("<td>" + rs.getString("payment_status") + "</td>");
                out.println("<td>" + rs.getDate("transaction_date") + "</td>");
                out.println("<td><a href='DeletePaymentServlet?payment_id=" + paymentId + "' onclick='return confirm(\"Are you sure you want to delete this payment record?\")' style='color:red;font-weight:bold;'>Delete</a></td>");
                out.println("</tr>");
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<tr><td colspan='7'>Error fetching payment history.</td></tr>");
        }
    }
}
