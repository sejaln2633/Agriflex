import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/SubmitRentalRequestServlet")
public class SubmitRentalRequestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        int customerId = Integer.parseInt(request.getParameter("customer_id"));
        int equipmentId = Integer.parseInt(request.getParameter("equipment_id"));
        int farmerId = Integer.parseInt(request.getParameter("farmer_id"));
        String startDate = request.getParameter("start_date");
        String endDate = request.getParameter("end_date");

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");

            // Insert rental request into the database
            String sql = "INSERT INTO rentals (customer_id, equipment_id, start_date, end_date, status, created_at) VALUES (?, ?, ?, ?, 'pending', NOW())";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, customerId);
            pst.setInt(2, equipmentId);
            pst.setString(3, startDate);
            pst.setString(4, endDate);

            int rowsInserted = pst.executeUpdate();

            if (rowsInserted > 0) {
                // After successful rental request, mark equipment as rented
                String updateSql = "UPDATE equipment SET availability_status = 'rented' WHERE equipment_id = ?";
                PreparedStatement updatePst = con.prepareStatement(updateSql);
                updatePst.setInt(1, equipmentId);
                updatePst.executeUpdate();

                out.println("<script>alert('Rental Request Submitted Successfully!'); window.location='customer-dashboard.html';</script>");
            } else {
                out.println("<script>alert('Failed to submit rental request. Please try again.'); window.history.back();</script>");
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<script>alert('Error processing request.'); window.history.back();</script>");
        }
    }
}
