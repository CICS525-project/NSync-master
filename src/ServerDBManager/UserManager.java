package ServerDBManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserManager {

    private static Connection con = DBProperties.establishConnection();

    public static boolean loginUser(String username, String password) {
        Connection con = DBProperties.establishConnection();
        boolean result = false;
        try {
            PreparedStatement ps = con
                    .prepareStatement("SELECT * FROM dblike_users WHERE user_id = ? AND password = ?");
            ps.setString(1, username);
            ps.setString(2, generatePasswordHash(password));
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
        try {
            con.close();// = DBProperties.establishConnection();
        } catch (SQLException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static boolean verifyUser(String username, String password) {
        Connection con = DBProperties.establishConnection();
        boolean result = false;
        try {
            PreparedStatement ps = con
                    .prepareStatement("SELECT * FROM dblike_users WHERE user_id = ? AND password = ?");
            ps.setString(1, username);
            ps.setString(2, password);
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
        try {
            con.close();// = DBProperties.establishConnection();
        } catch (SQLException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private static boolean checkIfUsernameOrEmailExists(String username, String email, int serverId) {
        Connection con = DBProperties.establishConnection(serverId);
        boolean result = false;
        try {
            PreparedStatement ps = con
                    .prepareStatement("SELECT * FROM dblike_users WHERE user_id = ? OR password = ?");
            ps.setString(1, username);
            ps.setString(2, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = true;
            } else {
                result = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result = true;
        }
        try {
            con.close();// = DBProperties.establishConnection();
        } catch (SQLException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private static boolean getPermissionToCreateUser(String username, String email) {
        for (int i = 1; i < 4; i++) {
            if (checkIfUsernameOrEmailExists(username, email, i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean createUser(String username, String password,
            String email) {
        Connection con = DBProperties.establishConnection();
        if (getPermissionToCreateUser(username, email)) {
            try {
                con.setAutoCommit(false);
                //Statement stmt = con.createStatement();
                PreparedStatement ps = con
                        .prepareStatement("INSERT INTO dblike_users (user_id, password, email) VALUES (?, ?, ?)");
                ps.setString(1, username);
                ps.setString(2, generatePasswordHash(password));
                ps.setString(3, email);
                ps.executeUpdate();
                con.commit();
            //stmt.close();
                // Provide a message when processing is complete.
                System.out.println("User " + username + "  " + password + " successfully created");
                try {
                    con.close();// = DBProperties.establishConnection();
                } catch (SQLException ex) {
                    Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                return true;
            } catch (Exception e) {
                try {
                    con.rollback();
                    con.close();
                } catch (SQLException ex) {
                    Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean createUserFromQueue(String username, String password,
            String email) {
        Connection con = DBProperties.establishConnection();
        try {
            con.setAutoCommit(false);
            //Statement stmt = con.createStatement();
            PreparedStatement ps = con
                    .prepareStatement("INSERT INTO dblike_users (user_id, password, email) VALUES (?, ?, ?)");
            ps.setString(1, username);
            ps.setString(2, generatePasswordHash(password));
            ps.setString(3, email);
            ps.executeUpdate();
            con.commit();
            //stmt.close();
            // Provide a message when processing is complete.
            System.out.println("User " + username + "  " + password + " successfully created");
            try {
                con.close();// = DBProperties.establishConnection();
            } catch (SQLException ex) {
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        } catch (Exception e) {
            try {
                con.rollback();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            e.printStackTrace();
            return false;
        }
    }

    public synchronized static String createQueueForUser(String username) {
        return null;
    }

    public static String getGeneratedPassword(String password) {
        return generatePasswordHash(password);
    }

    private static String generatePasswordHash(String s) {
        String salt = "Random$SaltValue#WithSpecialCharacters12@$@4&#%^$*";
        String password = org.apache.commons.codec.digest.DigestUtils.md5Hex(s
                + salt);
        return password;
    }

}
