import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/UpdateRentalStatusServlet")
public class UpdateRentalStatusServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int rentalId = Integer.parseInt(request.getParameter("rental_id"));
        String status = request.getParameter("status");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriflexdb", "root", "root");

            String query = "UPDATE rentals SET status=? WHERE rental_id=?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, status);
            pst.setInt(2, rentalId);

            int rowsUpdated = pst.executeUpdate();
            con.close();

            if (rowsUpdated > 0) {
                response.sendRedirect("view-rentals.html?success=" + status);
            } else {
                response.getWriter().println("<h3>Error: Unable to update status.</h3>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h3>Error processing request.</h3>");
        }
    }
}
