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

@WebServlet("/ApplyForRentServlet")
public class ApplyForRentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(); // ✅ Fixed session line

        // Ensure user is logged in
        Integer userId = (Integer) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("role");

        if (userId == null || !"customer".equalsIgnoreCase(userRole)) {
            response.sendRedirect("login.html");
            return;
        }

        // Get Equipment ID from request
        String equipmentIdStr = request.getParameter("equipment_id");
        if (equipmentIdStr == null) {
            out.println("<h3 style='color:red;'>Invalid request: Equipment ID is missing.</h3>");
            return;
        }

        int equipmentId = Integer.parseInt(equipmentIdStr);
        String equipmentName = "";
        String farmerName = "";
        int farmerId = 0;

        // Fetch Equipment Details
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");
            String sql = "SELECT e.name AS equipment_name, e.farmer_id, u.name AS farmer_name FROM equipment e JOIN users u ON e.farmer_id = u.id WHERE e.equipment_id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, equipmentId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                equipmentName = rs.getString("equipment_name");
                farmerName = rs.getString("farmer_name");
                farmerId = rs.getInt("farmer_id");
            }
            
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<h3>Error fetching equipment details.</h3>");
            return;
        }

        out.println("<html><head><title>Apply for Rent</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; text-align: center; background-color: #f4f4f4; }");
        out.println(".form-container { width: 50%; margin: auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0px 0px 10px gray; }");
        out.println("input, button { padding: 10px; width: 80%; margin-top: 10px; }");
        out.println("button { background: green; color: white; border: none; cursor: pointer; }");
        out.println("button:hover { background: darkgreen; }");
        out.println("</style>");

        // 🌟 JavaScript for date validation
        out.println("<script>");
        out.println("function validateDates() {");
        out.println("  var start = document.querySelector(\"input[name='start_date']\").value;");
        out.println("  var end = document.querySelector(\"input[name='end_date']\").value;");
        out.println("  var today = new Date().toISOString().split('T')[0];");

        out.println("  if (start < today || end < today) {");
        out.println("    alert('Please select valid future dates!'); return false;");
        out.println("  }");
        out.println("  if (end < start) {");
        out.println("    alert('End Date must be after Start Date!'); return false;");
        out.println("  }");
        out.println("  return true;");
        out.println("}");
        out.println("</script>");

        out.println("</head><body>");
        

        out.println("<div class='form-container'>");
        out.println("<h2>Rental Application</h2>");
        out.println("<form action='SubmitRentalRequestServlet' method='POST' onsubmit='return validateDates();'>");
        out.println("<input type='hidden' name='equipment_id' value='" + equipmentId + "'>");
        out.println("<input type='hidden' name='farmer_id' value='" + farmerId + "'>");
        out.println("<input type='hidden' name='customer_id' value='" + userId + "'>");
        out.println("<p>Equipment: <b>" + equipmentName + "</b></p>");
        out.println("<p>Farmer: <b>" + farmerName + "</b></p>");
        out.println("Start Date: <input type='date' name='start_date' required><br><br>");
        out.println("End Date: <input type='date' name='end_date' required><br><br>");
        out.println("<button type='submit'>Submit Request</button>");
        out.println("</form>");
        out.println("</div>");

        out.println("</body></html>");
    }
}