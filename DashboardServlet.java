

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/DashboardServlet")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userRole") == null) {
            response.sendRedirect("login.html");
            return;
        }
        
        String role = (String) session.getAttribute("userRole");
        if ("farmer".equals(role)) {
            response.sendRedirect("farmer-dashboard.html");
        } else if ("customer".equals(role)) {
            response.sendRedirect("customer-dashboard.html");
        } else {
            response.sendRedirect("login.html");
        }
    }
}
