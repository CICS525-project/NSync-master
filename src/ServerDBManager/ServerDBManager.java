package ServerDBManager;

import Controller.SendObject;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerDBManager {

    public static Connection connection = null;

    public static void startServerDB() {
        // Connection string for your SQL Database server 1.
        String connectionString = "jdbc:sqlserver://e55t52o9fy.database.windows.net:1433"
                + ";"
                + "database=db_like"
                + ";"
                + "user=db2@e55t52o9fy"
                + ";" + "password=NSyncgroup5";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("ClassNotFoundException " + cnfe.getMessage());
        }
        try {
            connection = DriverManager.getConnection(connectionString);

            //checking to see if the "files" table exists on the server DB
            DatabaseMetaData md = connection.getMetaData();
            ResultSet filesTable = md.getTables(null, null, "files", null);

            if (!filesTable.next()) {
                
                createFilesTable();
                System.out.println("works");
            }

        } catch (SQLException e) {
            System.out.println("Server DB could not be started");
            e.printStackTrace(System.out);
        }

    }

    private static void createFilesTable() {
        Statement stmt;
        try {
            stmt = connection.createStatement();
            
            String sqlString = "CREATE TABLE files(file_id VARCHAR(200),"
                    + "                    file_path   VARCHAR(200), "
                    + "                    file_name   VARCHAR(200), "
                    + "                    file_hash   VARCHAR(200), "
                    +"                      file_state   VARCHAR(200),"
                    + "                   last_update      TIMESTAMP, "
                    + "                   user_id	  VARCHAR(200), "
                    + "                   primary key(user_id, file_id)"
                    + "                  ) ";
            
            stmt.executeUpdate(sqlString);
           
        } catch (SQLException ex) {
            Logger.getLogger(ServerDBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*public static boolean updateDB(SendObject sendObject){
        
    }*/

    public static void main(String[] args) {
        ServerDBManager.startServerDB();
    }
}
