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

@WebServlet("/PaymentDashboardServlet")
public class PaymentDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Retrieve parameters
        String rentalId = request.getParameter("rental_id");
        String customerId = request.getParameter("customer_id");
        String equipmentId = request.getParameter("equipment_id");
        String farmerId = request.getParameter("farmer_id");

        // Ensure all parameters are present
        if (rentalId == null || customerId == null || equipmentId == null || farmerId == null) {
            out.println("<h3>Error: Missing required parameters.</h3>");
            return;
        }

        boolean isPaid = false;
        double amount = 0.0;

        try {
            // Database connection
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");

            // Calculate amount = rental_price * number_of_days
            String priceQuery = "SELECT e.rental_price, (DATEDIFF(r.end_date, r.start_date) + 1) AS rental_days " +
                    "FROM rentals r JOIN equipment e ON r.equipment_id = e.equipment_id " +
                    "WHERE r.rental_id = ?";

            PreparedStatement priceStmt = con.prepareStatement(priceQuery);
            priceStmt.setInt(1, Integer.parseInt(rentalId));
            ResultSet priceRs = priceStmt.executeQuery();

            if (priceRs.next()) {
                double rentalPrice = priceRs.getDouble("rental_price");
                int rentalDays = priceRs.getInt("rental_days");
                rentalDays = (rentalDays <= 0) ? 1 : rentalDays; // Ensure at least 1 day
                amount = rentalPrice * rentalDays;
            }

            // Check if the payment has already been made
            String checkPaymentQuery = "SELECT payment_status FROM payments WHERE rental_id = ? AND customer_id = ?";
            PreparedStatement checkPst = con.prepareStatement(checkPaymentQuery);
            checkPst.setInt(1, Integer.parseInt(rentalId));
            checkPst.setInt(2, Integer.parseInt(customerId));
            ResultSet rs = checkPst.executeQuery();

            if (rs.next()) {
                String paymentStatus = rs.getString("payment_status");
                if ("Paid".equalsIgnoreCase(paymentStatus)) {
                    isPaid = true;
                }
            }

            // Start HTML
            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'><head><meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Payment Summary</title>");
            out.println("<style>");
            out.println("body { margin:0; padding:0; font-family:'Segoe UI', sans-serif; background:linear-gradient(135deg,#667eea,#764ba2); color:#fff; display:flex; justify-content:center; align-items:center; height:100vh; }");
            out.println(".container { background:rgba(255,255,255,0.1); backdrop-filter:blur(10px); border-radius:15px; padding:30px; width:400px; box-shadow:0 8px 24px rgba(0,0,0,0.2); animation:fadeIn 1s ease; }");
            out.println("@keyframes fadeIn { from {opacity:0; transform:translateY(20px);} to {opacity:1; transform:translateY(0);} }");
            out.println("h2 { text-align:center; margin-bottom:20px; }");
            out.println("p { font-size:16px; margin:10px 0; }");
            out.println("form { display:flex; flex-direction:column; gap:15px; }");
            out.println("select, button { padding:10px; border:none; border-radius:8px; font-size:16px; }");
            out.println("select { background:#fff; color:#333; }");
            out.println("button { background:linear-gradient(135deg,#43e97b,#38f9d7); color:#000; cursor:pointer; transition:0.3s ease; font-weight:600; }");
            out.println("button:hover { transform:scale(1.05); box-shadow:0 6px 18px rgba(0,0,0,0.2); }");
            out.println(".paid-button { background-color:green; color:white; cursor:not-allowed; }");
            out.println("</style></head><body>");
            out.println("<div class='container'>");

            // Display payment summary
            out.println("<h2>Payment Summary</h2>");
            out.println("<p><strong>Rental ID:</strong> " + rentalId + "</p>");
            out.println("<p><strong>Customer ID:</strong> " + customerId + "</p>");
            out.println("<p><strong>Equipment ID:</strong> " + equipmentId + "</p>");
            out.println("<p><strong>Farmer ID:</strong> " + farmerId + "</p>");
            out.println("<p><strong>Total Amount to Pay:</strong> ₹" + amount + "</p>");

            // Payment confirmation form
            if (isPaid) {
                out.println("<h3 style='color:lightgreen;'>Payment Completed ✅</h3>");
                out.println("<button class='paid-button' disabled>Paid</button>");
            } else {
                out.println("<form action='ProcessPaymentServlet' method='POST'>");
                out.println("<input type='hidden' name='rental_id' value='" + rentalId + "'>");
                out.println("<input type='hidden' name='customer_id' value='" + customerId + "'>");
                out.println("<input type='hidden' name='equipment_id' value='" + equipmentId + "'>");
                out.println("<input type='hidden' name='farmer_id' value='" + farmerId + "'>");
                out.println("<input type='hidden' name='amount' value='" + amount + "'>");
                
                out.println("<label>Select Payment Method:</label>");
                out.println("<select name='payment_method' required>");
                out.println("<option value=''>-- Choose --</option>");
                out.println("<option value='UPI'>UPI</option>");
                out.println("<option value='Credit Card'>Credit Card</option>");
                out.println("<option value='Debit Card'>Debit Card</option>");
                out.println("</select>");

                out.println("<button type='submit'>Confirm Payment</button>");
                out.println("</form>");
            }

            out.println("</div></body></html>");
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<h3 style='color:red;'>Error processing payment status.</h3>");
        }
    }
}
