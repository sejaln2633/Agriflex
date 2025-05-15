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

@WebServlet("/BrowseEquipmentsServlet")
public class BrowseEquipmentsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String minPrice = request.getParameter("minPrice");
        String maxPrice = request.getParameter("maxPrice");
        String keyword = request.getParameter("search"); // search keyword from equipment name

        double min = (minPrice != null && !minPrice.isEmpty()) ? Double.parseDouble(minPrice) : 0;
        double max = (maxPrice != null && !maxPrice.isEmpty()) ? Double.parseDouble(maxPrice) : Double.MAX_VALUE;
        String searchKeyword = (keyword != null) ? keyword.trim() : "";

        try {
            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish Connection
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");

            // Updated SQL query with search and price filter
            String sql = "SELECT e.equipment_id, e.name, e.description, e.rental_price, e.availability_status, e.created_at, u.name AS farmer_name " +
                         "FROM equipment e " +
                         "JOIN users u ON e.farmer_id = u.id " +
                         "WHERE e.rental_price BETWEEN ? AND ? AND e.name LIKE ?";

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setDouble(1, min);
            pst.setDouble(2, max);
            pst.setString(3, "%" + searchKeyword + "%");
            ResultSet rs = pst.executeQuery();

            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                int equipmentId = rs.getInt("equipment_id");
                String status = rs.getString("availability_status");

                out.println("<tr>");
                out.println("<td>" + equipmentId + "</td>");
                out.println("<td>" + rs.getString("name") + "</td>");
                out.println("<td>" + rs.getString("description") + "</td>");
                out.println("<td>" + rs.getDouble("rental_price") + "</td>");
                out.println("<td>" + status + "</td>");
                out.println("<td>" + rs.getTimestamp("created_at") + "</td>");
                out.println("<td>" + rs.getString("farmer_name") + "</td>");

                // Apply button logic: Enable only if status is "available"
                out.println("<td>");
                if ("available".equalsIgnoreCase(status)) {
                    out.println("<form action='ApplyForRentServlet' method='GET'>");
                    out.println("<input type='hidden' name='equipment_id' value='" + equipmentId + "'>");
                    out.println("<button type='submit'>Apply for Rent</button>");
                    out.println("</form>");
                } else {
                    out.println("<button disabled>Not Available</button>");
                }
                out.println("</td>");
                out.println("</tr>");
            }

            if (!hasRecords) {
                out.println("<tr><td colspan='8' style='text-align:center;'>No Equipment Found Matching Criteria</td></tr>");
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<tr><td colspan='8'>Error fetching data</td></tr>");
        }
    }
}
