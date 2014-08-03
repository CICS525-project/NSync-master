package ServerDBManager;

import Controller.SendObject;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
                    + "                      file_state   VARCHAR(200),"
                    + "                   last_local_update      DATETIME, "
                    + "                   user_id	  VARCHAR(200), "
                    + "                   primary key(user_id, file_id)"
                    + "                  ) ";

            stmt.executeUpdate(sqlString);

        } catch (SQLException ex) {
            Logger.getLogger(ServerDBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean updateDB(SendObject sendObject) {

        return false;
    }

    public static boolean isIDInDB(String file_id) {
        boolean result = false;

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM files WHERE file_id = ?");
            ps.setString(1, file_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = true;
            } else {
                result = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public static int serverInsert(String file_id, String file_path, String file_name, String file_hash, String user_id, java.sql.Timestamp last_local_update) {
        int result = -1;

        setConnection();

        if (!isIDInDB(file_id)) {
            try {
                PreparedStatement ps = connection
                        .prepareStatement("INSERT INTO files(file_id, file_path, file_name, file_hash, last_local_update, user_id) VALUES (?,?,?,?,?,?)");
                ps.setString(1, file_id);
                ps.setString(2, file_path);
                ps.setString(3, file_name);
                ps.setString(4, file_hash);
                ps.setTimestamp(5, last_local_update);
                ps.setString(6, user_id);
                result = ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
                result = -1;
            }
        } else {
            result = -1;
        }

        return result;
    }

    public static int serverModify(String file_id, String file_hash, java.sql.Timestamp last_local_update) {
        int result = -1;
        setConnection();

        if (isIDInDB(file_id)) {
            try {
                PreparedStatement ps = connection
                        .prepareStatement("UPDATE files SET last_local_update = ?, file_hash = ? WHERE file_id = ?");
                ps.setTimestamp(1, last_local_update);
                ps.setString(2, file_hash);
                ps.setString(3, file_id);
                result = ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
                result = -1;
            }
        }

        return result;
    }

    /*
     public static int serverRename(String file_id, String file_name, String new_file_name, String file_path, java.sql.Timestamp last_local_update, boolean is_folder) 
     {
     int result = -1;
     setConnection();
     PreparedStatement ps = null;
     String root_path = "";

     //update the renamed folder/file entry
     try 
     {
     ps = connection.prepareStatement("UPDATE files "
     + " SET last_local_update = ? "
     + "    ,file_name = ? "
     + "WHERE file_path = ? "
     + "  AND file_name = ?");
     ps.setTimestamp(1, last_local_update);
     ps.setString(2, new_file_name);
     ps.setString(3, file_path);
     ps.setString(4, file_name);
     result = ps.executeUpdate();
     } 
     catch (Exception e) 
     {
     e.printStackTrace();
     result = -1;
     }
     if (is_folder) 
     {
     //for folder entry cascade rename to child entries
     if(file_path.trim().length() == 0)
     {
     root_path = "";
     }
     else
     {
     root_path = file_path + FileSystems.getDefault().getSeparator();
     }
     try 
     {
     System.out.println("________________________________________________________________________");
     System.out.println("root_path + file_name + FileSystems.getDefault().getSeparator() ==> "+root_path + file_name + FileSystems.getDefault().getSeparator() );
     System.out.println("root_path + new_file_name + FileSystems.getDefault().getSeparator() ==> " + root_path + new_file_name + FileSystems.getDefault().getSeparator() );
     System.out.println("root_path + file_name + FileSystems.getDefault().getSeparator() + % ==> " + root_path + file_name + FileSystems.getDefault().getSeparator() + "%");
				
     ps = con.prepareStatement("UPDATE files "
     + " SET file_path = replace(file_path,  ?, ?) "
     + "WHERE file_path LIKE ?");
     ps.setString(1, root_path + file_name + FileSystems.getDefault().getSeparator() );
     ps.setString(2, root_path + new_file_name + FileSystems.getDefault().getSeparator() );
     ps.setString(3, root_path + file_name + FileSystems.getDefault().getSeparator() + "%");
     result = ps.executeUpdate();
     System.out.println("________________________________________________________________________");
     } 
     catch (Exception e) 
     {
     e.printStackTrace();
     result = -1;
     }
     try 
     {
     System.out.println("________________________________________________________________________");

     System.out.println("root_path + file_name ==> "+root_path + file_name );
     System.out.println("root_path + new_file_name ==> " + root_path + new_file_name );
     System.out.println("root_path + file_name ==> " + root_path + file_name);
				
     ps = con.prepareStatement("UPDATE files "
     + " SET file_path = replace(file_path,  ?, ?) "
     + "WHERE file_path = ?");
     ps.setString(1, root_path + file_name);
     ps.setString(2, root_path + new_file_name);
     ps.setString(3, root_path + file_name);
     result = ps.executeUpdate();
     System.out.println("________________________________________________________________________");
     } 
     catch (Exception e) 
     {
     e.printStackTrace();
     result = -1;
     }
     }

     return result;
     }
     */
    public static int serverDelete(String file_id) {
        int result = -1;

        setConnection();
        try {
            PreparedStatement ps = connection
                    .prepareStatement("DELETE FROM files WHERE file_id = ?");
            ps.setString(1, file_id);
            result = ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            result = -1;
        }
        return result;
    }

    private static void setConnection() {
        if (connection == null) {
            startServerDB();
        }

    }

    public static void main(String[] args) {
        ServerDBManager.startServerDB();

        java.sql.Timestamp TS = new java.sql.Timestamp(new java.util.Date().getTime());
        /*
         System.out.println("testing insert");
         int result = serverInsert("file_id", "file_path", "file_name", "file_hash", "user_id", TS );
         System.out.println(result);
         
        System.out.println("testing modify");
        int result2 = serverModify("file_id", "file_hash", TS);
        System.out.println(result2);
        */
        System.out.println("testing delete");
        int result3 = serverDelete("file_id");
        System.out.println(result3);
    }

}
