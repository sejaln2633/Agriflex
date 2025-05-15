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

@WebServlet("/MyRentalsServlet")
public class MyRentalsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();

        // Get logged-in customer ID
        Integer customerId = (Integer) session.getAttribute("user_id");

        if (customerId == null || !"customer".equals(session.getAttribute("user_role"))) {
            response.sendRedirect("login.html");
            return;
        }

        out.println("<html><head><title>My Rentals</title></head><body>");
        out.println("<h2>My Rentals</h2>");
        out.println("<table border='1'><tr><th>Equipment</th><th>Start Date</th><th>End Date</th><th>Status</th></tr>");

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");

            String sql = "SELECT e.name, r.start_date, r.end_date, r.status FROM rentals r " +
                         "JOIN equipment e ON r.equipment_id = e.equipment_id WHERE r.customer_id = ?";

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                out.println("<tr><td>" + rs.getString("name") + "</td><td>" + rs.getString("start_date") + "</td><td>" + rs.getString("end_date") + "</td><td>" + rs.getString("status") + "</td></tr>");
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<p>Error loading rentals</p>");
        }

        out.println("</table></body></html>");
    }
}
