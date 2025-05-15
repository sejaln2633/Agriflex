import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // ✅ Database Connection Details
    private static final String URL = "jdbc:mysql://localhost:3306/agriflexdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"; 
    private static final String USER = "root";  // Change to your MySQL username
    private static final String PASSWORD = "root";  // Change to your MySQL password
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";  

    // ✅ Method to Establish a Database Connection
    public static Connection getConnection() {
        Connection conn = null;
        try {
            System.out.println("🔍 Loading MySQL JDBC Driver...");
            Class.forName(DRIVER); // Load MySQL JDBC Driver

            System.out.println("🔍 Connecting to Database...");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);

            if (conn != null) {
                System.out.println("✅ Database Connection Established Successfully!");
            } else {
                System.out.println("❌ Connection Failed! Returning null.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Database Driver Not Found! Check if MySQL Connector/J is added.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Failed to Connect to Database! Check if MySQL Server is running and credentials are correct.");
            e.printStackTrace();
        }
        return conn;  // If connection fails, returns null
    }

    // ✅ Method to Close Database Connection
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("✅ Database Connection Closed!");
            } catch (SQLException e) {
                System.err.println("❌ Error Closing Connection!");
                e.printStackTrace();
            }
        }
    }

    // ✅ Main method for Testing Connection
    public static void main(String[] args) {
        Connection testConn = getConnection();
        if (testConn != null) {
            System.out.println("🎉 Connection Test Successful!");
            closeConnection(testConn);
        } else {
            System.out.println("❌ Connection Test Failed!");
        }
    }
}
