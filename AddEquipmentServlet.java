import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AddEquipmentServlet")
public class AddEquipmentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null || !"farmer".equals(session.getAttribute("role"))) {
            response.getWriter().println("<h3>Error: You must be logged in as a farmer to add equipment.</h3>");
            return;
        }

        int farmerId = (int) session.getAttribute("userId"); // Get the logged-in farmer ID
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        double rentalPrice = Double.parseDouble(request.getParameter("rental_price"));
        String availabilityStatus = request.getParameter("availability_status"); // "available" or "unavailable"

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");

            String query = "INSERT INTO equipment (name, description, rental_price, availability_status, farmer_id, created_at) VALUES (?, ?, ?, ?, ?, NOW())";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, name);
            pst.setString(2, description);
            pst.setDouble(3, rentalPrice);
            pst.setString(4, availabilityStatus);
            pst.setInt(5, farmerId);  // Assign logged-in farmer's ID

            int rowsInserted = pst.executeUpdate();
            con.close();

            if (rowsInserted > 0) {
                response.getWriter().println("<!DOCTYPE html>" +
                	    	    "<html><head><title>Success</title>" +
                	    	    "<style>" +
                	    	    "body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #e0f7fa, #80deea); height: 100vh; display: flex; justify-content: center; align-items: center; }" +
                	    	    ".message-box { background: white; padding: 40px 30px; border-radius: 12px; box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2); text-align: center; }" +
                	    	    ".message-box h1 { color: #2e7d32; font-size: 26px; margin-bottom: 10px; }" +
                	    	    ".message-box p { font-size: 16px; color: #555; }" +
                	    	    "a { text-decoration: none; display: inline-block; margin-top: 15px; background-color: #4caf50; color: white; padding: 10px 20px; border-radius: 8px; transition: 0.3s; }" +
                	    	    "a:hover { background-color: #388e3c; }" +
                	    	    "</style>" +
                	    	    "</head><body>" +
                	    	    "<div class='message-box'>" +
                	    	    "<h1>✅ Equipment added successfully!</h1>" +
                	    	    "<p>Your equipment has been added to the system.</p>" +
                	    	    "<a href='farmer-dashboard.html'>Go to Dashboard</a>" +
                	    	    "</div>" +
                	    	    "</body></html>"
                	    	);

            } else {
                response.getWriter().println("<h3>Failed to add equipment.</h3>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h3>Error adding equipment.</h3>");
        }
    }
}
