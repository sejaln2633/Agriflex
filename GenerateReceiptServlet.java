import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/GenerateReceiptServlet")
public class GenerateReceiptServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int rentalId = Integer.parseInt(request.getParameter("rental_id"));

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");

            String sql = "SELECT e.name AS equipment_name, u.name AS farmer_name, " +
                    "r.start_date, r.end_date, e.rental_price, " +
                    "(DATEDIFF(r.end_date, r.start_date) + 1) AS rental_days, " +
                    "p.payment_method, p.transaction_date " +
                    "FROM payments p " +
                    "JOIN rentals r ON p.rental_id = r.rental_id " +
                    "JOIN equipment e ON r.equipment_id = e.equipment_id " +
                    "JOIN users u ON e.farmer_id = u.id " +
                    "WHERE p.rental_id = ?";

            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, rentalId);
            ResultSet rs = stmt.executeQuery();

            // Get current system time
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String formattedNow = now.format(formatter);

            PrintWriter out = response.getWriter();
            response.setContentType("text/html");

            out.println("<html><head><title>Payment Receipt</title>");
            out.println("<style>");
            out.println("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f2f7ff; color: #333; padding: 40px 20px; display: flex; justify-content: center; }");
            out.println(".receipt { background-color: #fff; padding: 30px; border-radius: 15px; max-width: 600px; width: 100%; box-shadow: 0 10px 30px rgba(0,0,0,0.1); animation: slideIn 0.5s ease; }");
            out.println("h2 { text-align: center; color: #2c3e50; margin-bottom: 25px; }");
            out.println("p { font-size: 16px; margin: 10px 0; border-bottom: 1px dashed #ccc; padding-bottom: 5px; }");
            out.println("strong { color: #555; }");
            out.println("button { display: block; margin: 20px auto 10px; padding: 12px 20px; background-color: #2980b9; color: white; border: none; font-size: 16px; border-radius: 8px; cursor: pointer; transition: background 0.3s ease; }");
            out.println("button:hover { background-color: #1c5980; }");
            out.println("@media print { button { display: none; } body { background: none; padding: 0; } .receipt { box-shadow: none; border: none; padding: 0; } }");
            out.println("@keyframes slideIn { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }");
            out.println("</style></head>");
            out.println("<body><div class='receipt'><h2>Payment Receipt</h2>");

            if (rs.next()) {
                Timestamp transactionTimestamp = rs.getTimestamp("transaction_date");
                String transactionDateTime = transactionTimestamp.toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                out.println("<p><strong>Equipment:</strong> " + rs.getString("equipment_name") + "</p>");
                out.println("<p><strong>Farmer:</strong> " + rs.getString("farmer_name") + "</p>");
                out.println("<p><strong>Rental Period:</strong> " + rs.getDate("start_date") + " to " + rs.getDate("end_date") + "</p>");
                double pricePerDay = rs.getDouble("rental_price");
                int rentalDays = rs.getInt("rental_days");
                double totalAmount = pricePerDay * rentalDays;
                out.println("<p><strong>Amount Paid:</strong> ₹" + totalAmount + " (" + rentalDays + " days × ₹" + pricePerDay + ")</p>");

                out.println("<p><strong>Payment Method:</strong> " + rs.getString("payment_method") + "</p>");
                out.println("<p><strong>Transaction Date and Time:</strong> " + formattedNow + "</p>");
                out.println("<p><strong>Receipt Generated At:</strong> " + formattedNow + "</p>");
                out.println("<button onclick='window.print()'>Print</button>");
                out.println("<button onclick=\"location.href='browse-equipment.html'\">Back</button>");
            } else {
                out.println("<p>Receipt not found!</p>");
                out.println("<button onclick=\"location.href='browse-equipment.html'\">Back</button>");
            }

            out.println("</div></body></html>");
            conn.close();

        } catch (Exception e) {
            response.getWriter().println("Error: " + e.getMessage());
        }
    }
}
