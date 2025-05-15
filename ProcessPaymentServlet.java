import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ProcessPaymentServlet")
public class ProcessPaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 🔐 Get parameters
            int rentalId = Integer.parseInt(request.getParameter("rental_id"));
            int customerId = Integer.parseInt(request.getParameter("customer_id"));
            int equipmentId = Integer.parseInt(request.getParameter("equipment_id"));
            int farmerId = Integer.parseInt(request.getParameter("farmer_id"));
            String paymentMethod = request.getParameter("payment_method");

            String transactionDate = LocalDate.now().toString();
            double totalAmount = 0.0;

            // 🧠 Connect DB
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");

            // Check if payment already exists
            String checkSql = "SELECT * FROM payments WHERE rental_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, rentalId);
            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next()) {
                response.setContentType("text/html; charset=UTF-8");
                response.getWriter().println("⚠️ Payment already done!<br><br>");
                response.getWriter().println("<a href='GenerateReceiptServlet?rental_id=" + rentalId + "'>➡️ View Receipt</a>");
                conn.close();
                return;
            }

            // 📅 Get rental dates
            LocalDate startDate = null, endDate = null;
            String rentalSql = "SELECT start_date, end_date FROM rentals WHERE rental_id = ?";
            PreparedStatement rentalStmt = conn.prepareStatement(rentalSql);
            rentalStmt.setInt(1, rentalId);
            ResultSet rentalRs = rentalStmt.executeQuery();
            if (rentalRs.next()) {
                startDate = rentalRs.getDate("start_date").toLocalDate();
                endDate = rentalRs.getDate("end_date").toLocalDate();
            } else {
                response.getWriter().println("Rental not found.");
                conn.close();
                return;
            }

            // 🧮 Calculate duration including end date
            long durationDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            if (durationDays <= 0) durationDays = 1;

            // 💸 Get rental_price
            String priceSql = "SELECT rental_price FROM equipment WHERE equipment_id = ?";
            PreparedStatement priceStmt = conn.prepareStatement(priceSql);
            priceStmt.setInt(1, equipmentId);
            ResultSet priceRs = priceStmt.executeQuery();
            double rentalPrice = 0.0;
            if (priceRs.next()) {
                rentalPrice = priceRs.getDouble("rental_price");
                totalAmount = rentalPrice * durationDays;
            } else {
                response.getWriter().println("Equipment not found.");
                conn.close();
                return;
            }

            // 💾 Insert payment
            String insertSql = "INSERT INTO payments (rental_id, customer_id, farmer_id, amount, payment_status, payment_method, transaction_date) "
                    + "VALUES (?, ?, ?, ?, 'Completed', ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, rentalId);
            insertStmt.setInt(2, customerId);
            insertStmt.setInt(3, farmerId);
            insertStmt.setDouble(4, totalAmount);
            insertStmt.setString(5, paymentMethod);
            insertStmt.setString(6, transactionDate);
            insertStmt.executeUpdate();

            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<html><head><title>Payment Confirmation</title>");
            out.println("<style>");
            out.println("body { font-family: 'Segoe UI', sans-serif; background-color: #f0f4f8; margin: 0; padding: 0; display: flex; align-items: center; justify-content: center; height: 100vh; }");
            out.println(".confirmation-box { background: #ffffff; padding: 30px; border-radius: 15px; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); text-align: center; max-width: 500px; }");
            out.println(".confirmation-box h2 { color: #28a745; margin-bottom: 15px; }");
            out.println(".amount { font-size: 20px; color: #333; margin: 10px 0; }");
            out.println(".receipt-link { display: inline-block; margin-top: 20px; padding: 12px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 8px; transition: background-color 0.3s ease; }");
            out.println(".receipt-link:hover { background-color: #0056b3; }");
            out.println("</style>");
            out.println("</head><body>");

            out.println("<div class='confirmation-box'>");
            out.println("<h2>✅ Payment Successful!</h2>");
            out.println("<p class='amount'>💰 Amount Paid: ₹" + totalAmount + "</p>");
            out.println("<p class='amount'>🗓 Duration: " + durationDays + " day(s)</p>");
            out.println("<a class='receipt-link' href='GenerateReceiptServlet?rental_id=" + rentalId + "'>➡️ View Receipt</a>");
            out.println("</div>");

            out.println("</body></html>");


            conn.close();
        } catch (Exception e) {
            response.getWriter().println("Payment Error: " + e.getMessage());
        }
    }
}
