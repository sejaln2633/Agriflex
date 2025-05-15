import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ViewMyRentalsServlet")
public class ViewMyRentalsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            out.println("<tr><td colspan='7'>Please log in to view your rentals.</td></tr>");
            return;
        }

        int customerId = (int) session.getAttribute("userId");

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");

            String query = "SELECT r.rental_id, e.name AS equipment_name, r.start_date, r.end_date, r.status, " +
                           "u.name AS farmer_name, u.id AS farmer_id, e.equipment_id, e.rental_price " +
                           "FROM rentals r " +
                           "JOIN equipment e ON r.equipment_id = e.equipment_id " +
                           "JOIN users u ON e.farmer_id = u.id " +
                           "WHERE r.customer_id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int rentalId = rs.getInt("rental_id");
                int farmerId = rs.getInt("farmer_id");
                int equipmentId = rs.getInt("equipment_id");
                double amount = rs.getDouble("rental_price");
                String status = rs.getString("status");

                // Check payment existence from payments table
                boolean isPaid = false;
                String method = "", date = "";
                double paidAmount = 0.0;
                String payQuery = "SELECT amount, payment_method, transaction_date FROM payments WHERE rental_id = ? AND customer_id = ? LIMIT 1";
                PreparedStatement payStmt = con.prepareStatement(payQuery);
                payStmt.setInt(1, rentalId);
                payStmt.setInt(2, customerId);
                ResultSet payRs = payStmt.executeQuery();
                if (payRs.next()) {
                    isPaid = true;
                    paidAmount = payRs.getDouble("amount");
                    method = payRs.getString("payment_method");
                    Timestamp ts = payRs.getTimestamp("transaction_date");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    date = sdf.format(ts);
                }

                payRs.close();
                payStmt.close();

                out.println("<tr>");
                out.println("<td>" + rs.getString("equipment_name") + "</td>");
                out.println("<td>" + rs.getString("farmer_name") + "</td>");
                out.println("<td>" + rs.getDate("start_date") + "</td>");
                out.println("<td>" + rs.getDate("end_date") + "</td>");
                out.println("<td class='" + getStatusClass(status) + "'>" + status + "</td>");
                out.println("<td>");

                if ("approved".equalsIgnoreCase(status)) {
                    if (isPaid) {
                        out.println("<button disabled style='background-color:green; color:white;'>Paid</button>");
                        out.println("<br><small>Amount: ₹" + paidAmount + "</small>");
                        out.println("<br><small>Method: " + method + "</small>");
                        out.println("<br><small>Date: " + date + "</small>");

                    } else {
                        out.println("<form action='PaymentDashboardServlet' method='GET'>");
                        out.println("<input type='hidden' name='rental_id' value='" + rentalId + "'>");
                        out.println("<input type='hidden' name='customer_id' value='" + customerId + "'>");
                        out.println("<input type='hidden' name='equipment_id' value='" + equipmentId + "'>");
                        out.println("<input type='hidden' name='farmer_id' value='" + farmerId + "'>");
                        out.println("<input type='hidden' name='amount' value='" + amount + "'>");
                        out.println("<button type='submit'>Pay Now</button>");
                        out.println("</form>");
                    }
                } else {
                    out.println("N/A");
                }

                out.println("</td></tr>");
            }

            rs.close();
            pst.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<tr><td colspan='7'>Error fetching rental data.</td></tr>");
        }
    }

    private String getStatusClass(String status) {
        switch (status.toLowerCase()) {
            case "pending": return "pending";
            case "approved": return "approved";
            case "rejected": return "rejected";
            default: return "";
        }
    }
}
