import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet("/GenerateReportServlet")
public class GenerateReportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriflexdb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String reportType = request.getParameter("reportType");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        // Placeholder for admin ID (Replace with session-based retrieval in production)
        int adminId = 1;

        String tableName = "";
        String dateColumn = "";

        // Determine which table and column to use
        switch (reportType) {
            case "users":
                tableName = "users";
                dateColumn = "created_at";
                break;
            case "equipment":
                tableName = "equipment";
                dateColumn = "created_at";
                break;
            case "rentals":
                tableName = "rentals";
                dateColumn = "start_date";
                break;
            case "payments":
                tableName = "payments";
                dateColumn = "transaction_date";
                break;
            default:
                out.println("<p style='color:red;'>Invalid report type selected.</p>");
                return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            // Build dynamic query
            String query = "SELECT * FROM " + tableName;
            PreparedStatement pst;

            boolean filterByDate = (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty());

            if (filterByDate) {
                query += " WHERE " + dateColumn + " BETWEEN ? AND ?";
                pst = conn.prepareStatement(query);
                pst.setString(1, startDate + " 00:00:00");
                pst.setString(2, endDate + " 23:59:59");
            } else {
                pst = conn.prepareStatement(query);
            }

            // Log the report generation in reports table
            String logQuery = "INSERT INTO reports (admin_id, report_type, report_data, created_at) VALUES (?, ?, ?, ?)";
            PreparedStatement logStmt = conn.prepareStatement(logQuery);
            logStmt.setInt(1, adminId);
            logStmt.setString(2, reportType);
            logStmt.setString(3, "Report generated for table: " + tableName + " from " + startDate + " to " + endDate);
            logStmt.setString(4, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            logStmt.executeUpdate();

            // Display the report
            ResultSet rs = pst.executeQuery();

            out.println("<table class='report-table'><tr>");
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                out.println("<th>" + meta.getColumnName(i) + "</th>");
            }
            out.println("</tr>");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                out.println("<tr>");
                for (int i = 1; i <= columnCount; i++) {
                    out.println("<td>" + rs.getString(i) + "</td>");
                }
                out.println("</tr>");
            }

            if (!hasData) {
                out.println("<tr><td colspan='" + columnCount + "' style='text-align:center;'>No records found for the selected range.</td></tr>");
            }

            out.println("</table>");

            conn.close();
        } catch (Exception e) {
            out.println("<p style='color:red;'>Error: " + e.getMessage() + "</p>");
            e.printStackTrace();
        }
    }
}
