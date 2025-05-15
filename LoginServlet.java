import java.io.IOException;
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

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Validate input fields
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            response.getWriter().println("<h3>Email and Password are required!</h3>");
            return;
        }

        try {
            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to MySQL
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");

            // Fetch user details from the database
            String query = "SELECT id, name, role FROM users WHERE email=? AND password=?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, email);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id"); // Fetch user ID
                String userName = rs.getString("name"); // Fetch user name
                String role = rs.getString("role").toLowerCase(); // Convert role to lowercase for consistency

                // Store user info in session
                HttpSession session = request.getSession();
                session.setAttribute("userId", userId);
                session.setAttribute("userName", userName);
                session.setAttribute("role", role);

                // Debugging: Print session values in console (Remove this in production)
                System.out.println("User ID: " + session.getAttribute("userId"));
                System.out.println("User Name: " + session.getAttribute("userName"));
                System.out.println("User Role: " + session.getAttribute("role"));

                // Redirect based on user role
                if ("admin".equals(role)) {
                    response.sendRedirect("admin-dashboard.html");
                } else if ("farmer".equals(role)) {
                    response.sendRedirect("farmer-dashboard.html");
                } else if ("customer".equals(role)) {
                    response.sendRedirect("customer-dashboard.html"); // Direct to Browse Equipments
                } else {
                    response.getWriter().println("<h3>Invalid user role!</h3>");
                }
            } else {
                response.getWriter().println("<h3>Invalid Email or Password</h3>");
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h3>Error: " + e.getMessage() + "</h3>");
        }
    }
}
