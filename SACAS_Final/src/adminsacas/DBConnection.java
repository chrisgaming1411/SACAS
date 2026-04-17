package adminsacas;

import java.sql.*;

/**
 * DBConnection – central JDBC helper.
 *
 * SETUP:
 *  1. Run sacas_db.sql in MySQL to create the database.
 *  2. Change DB_PASS below if your MySQL root has a password.
 *  3. Add mysql-connector-j-*.jar to NetBeans project Libraries
 *     (Right-click project → Properties → Libraries → Add JAR/Folder)
 *     Download: https://dev.mysql.com/downloads/connector/j/
 */
public class DBConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/sacas_db"
                                     + "?useSSL=false&allowPublicKeyRetrieval=true"
                                     + "&serverTimezone=Asia/Manila";
    private static final String USER = "root";
    private static final String PASS = "";   // ← change if needed

    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) {
            System.err.println("[SACAS] MySQL driver not found – add mysql-connector-j to Libraries");
        }
    }

    /** Returns a fresh connection. Caller must close it. */
    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Quick sanity test – run DBConnection.main() to verify. */
    public static void main(String[] args) {
        try (Connection c = get()) {
            System.out.println("Connected to " + c.getMetaData().getURL());
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
}
