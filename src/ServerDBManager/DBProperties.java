package ServerDBManager;

import Controller.ServerProperties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBProperties {

	public static Connection establishConnection() {
		// Connection string for your SQL Database server 1.
		String connectionString = Communication.Connection.getDBConnectionString(ServerProperties.serverId);
		Connection connection = null; // For making the connections
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		}

		catch (ClassNotFoundException cnfe) {
			System.out.println("ClassNotFoundException " + cnfe.getMessage());
		}
		try {
			connection = DriverManager.getConnection(connectionString);
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
		return connection;
	}
        
        public static Connection establishConnection(int serverId) {
		// Connection string for your SQL Database server 1.
		String connectionString = Communication.Connection.getDBConnectionString(serverId);
		Connection connection = null; // For making the connections
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		}

		catch (ClassNotFoundException cnfe) {
			System.out.println("ClassNotFoundException " + cnfe.getMessage());
		}
		try {
			connection = DriverManager.getConnection(connectionString);
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
		return connection;
	}

	public static void main(String[] args) {
		DBProperties.establishConnection();
	}
}