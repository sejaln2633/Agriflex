import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/view-payments")
public class AdminPaymentsServlet extends HttpServlet {
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

            // ✅ Corrected `e.id` → `e.equipment_id`
            String query = "SELECT p.payment_id, p.rental_id, p.customer_id, cu.name AS customer_name, " +
                           "p.farmer_id, f.name AS farmer_name, e.name AS equipment_name, p.amount, " +
                           "p.payment_status, p.payment_method, p.transaction_date, " +
                           "r.start_date, r.end_date, r.status " +
                           "FROM payments p " +
                           "INNER JOIN rentals r ON p.rental_id = r.rental_id " +  
                           "INNER JOIN equipment e ON r.equipment_id = e.equipment_id " +  // ✅ FIXED COLUMN
                           "INNER JOIN users cu ON p.customer_id = cu.id " +  
                           "INNER JOIN users f ON p.farmer_id = f.id";

            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            // ✅ HTML Output
            out.println("<html><head><title>View Payments</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; background-color: #f4f4f4; text-align: center; }");
            out.println(".container { width: 90%; margin: auto; padding: 20px; background: white; box-shadow: 0px 0px 10px gray; }");
            out.println("h2 { color: #333; } table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
            out.println("th, td { padding: 12px; border: 1px solid #ddd; text-align: left; }");
            out.println("th { background: #28a745; color: white; }");
            out.println(".not-paid { color: red; font-weight: bold; } .paid { color: green; font-weight: bold; }");
            out.println("</style></head><body>");
            out.println("<div class='container'><h2>Payment Transactions</h2>");
            out.println("<table><tr><th>Payment ID</th><th>Rental ID</th><th>Customer</th><th>Farmer</th><th>Equipment</th><th>Start Date</th><th>End Date</th><th>Status</th><th>Amount</th><th>Payment Status</th><th>Payment Method</th><th>Transaction Date</th></tr>");

            while (rs.next()) {
                int paymentId = rs.getInt("payment_id");
                int rentalId = rs.getInt("rental_id");
                String customerName = rs.getString("customer_name");
                String farmerName = rs.getString("farmer_name");
                String equipmentName = rs.getString("equipment_name");
                String startDate = rs.getString("start_date");
                String endDate = rs.getString("end_date");
                String rentalStatus = rs.getString("status");
                double amount = rs.getDouble("amount");
                String paymentStatus = rs.getString("payment_status");
                String paymentMethod = rs.getString("payment_method");
                String transactionDate = rs.getString("transaction_date");

                String statusClass = paymentStatus.equalsIgnoreCase("paid") ? "paid" : "not-paid";

                out.println("<tr>");
                out.println("<td>" + paymentId + "</td>");
                out.println("<td>" + rentalId + "</td>");
                out.println("<td>" + customerName + "</td>");
                out.println("<td>" + farmerName + "</td>");
                out.println("<td>" + equipmentName + "</td>");
                out.println("<td>" + startDate + "</td>");
                out.println("<td>" + endDate + "</td>");
                out.println("<td>" + rentalStatus + "</td>");
                out.println("<td>$" + amount + "</td>");
                out.println("<td class='" + statusClass + "'>" + paymentStatus + "</td>");
                out.println("<td>" + paymentMethod + "</td>");
                out.println("<td>" + transactionDate + "</td>");
                out.println("</tr>");
            }

            out.println("</table>");
            out.println("<br><a href='admin-dashboard.html'>Back to Dashboard</a>");
            out.println("</div></body></html>");

            conn.close();
        } catch (Exception e) {
            out.println("<p>Error: " + e.getMessage() + "</p>");
        }
    }
}
