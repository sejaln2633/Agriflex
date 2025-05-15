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

@WebServlet("/ViewRentalRequestsServlet")
public class ViewRentalRequestsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null || !"farmer".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.html");
            return;
        }

        int farmerId = (int) session.getAttribute("userId");

        out.println("<html><head><title>View Rental Requests</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; text-align: center; background-color: #f4f4f4; }");
        out.println(".container { width: 80%; margin: auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0px 0px 10px gray; }");
        out.println("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        out.println("th, td { padding: 10px; border: 1px solid #ddd; text-align: center; }");
        out.println("th { background-color: green; color: white; }");
        out.println("button { padding: 5px 10px; margin: 5px; border: none; cursor: pointer; border-radius: 5px; }");
        out.println(".approve { background-color: #28a745; color: white; }");
        out.println(".reject { background-color: #dc3545; color: white; }");
        out.println("</style>");
        out.println("</head><body>");
        
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");
            String sql = "SELECT r.rental_id, u.name AS customer_name, e.name AS equipment_name, r.start_date, r.end_date, r.status " +
                         "FROM rentals r JOIN users u ON r.customer_id = u.id " +
                         "JOIN equipment e ON r.equipment_id = e.equipment_id " +
                         "WHERE e.farmer_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, farmerId);
            ResultSet rs = pst.executeQuery();

            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                int rentalId = rs.getInt("rental_id");
                String customerName = rs.getString("customer_name");
                String equipmentName = rs.getString("equipment_name");
                String startDate = rs.getString("start_date");
                String endDate = rs.getString("end_date");
                String status = rs.getString("status");

                out.println("<tr>");
                out.println("<td>" + rentalId + "</td>");
                out.println("<td>" + customerName + "</td>");
                out.println("<td>" + equipmentName + "</td>");
                out.println("<td>" + startDate + "</td>");
                out.println("<td>" + endDate + "</td>");
                out.println("<td>" + status + "</td>");
                out.println("<td>");
                if ("pending".equalsIgnoreCase(status)) {
                    out.println("<form action='UpdateRentalStatusServlet' method='POST' style='display:inline;'>");
                    out.println("<input type='hidden' name='rental_id' value='" + rentalId + "'>");
                    out.println("<button type='submit' name='status' value='approved' class='approve'>Approve</button>");
                    out.println("</form>");
                    out.println("<form action='UpdateRentalStatusServlet' method='POST' style='display:inline;'>");
                    out.println("<input type='hidden' name='rental_id' value='" + rentalId + "'>");
                    out.println("<button type='submit' name='status' value='rejected' class='reject'>Reject</button>");
                    out.println("</form>");
                } else {
                    out.println("No Action Required");
                }
                out.println("</td>");
                out.println("</tr>");
            }

            if (!hasRecords) {
                out.println("<tr><td colspan='7'>No rental requests found.</td></tr>");
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<tr><td colspan='7'>Error fetching rental requests.</td></tr>");
        }

        out.println("</table></div></body></html>");
    }
} 