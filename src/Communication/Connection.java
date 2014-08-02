package Communication;

import Communication.NsyncServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.rmi.ssl.SslRMIClientSocketFactory;

public class Connection {

    public static boolean watchFolder = true;

    public static int serverPort = 9005;

    public static int serverId = getRandomServer();

    public static NsyncServerInterface server;

    private static int getRandomServer() {
        Random rand = new Random();
        int n = rand.nextInt(3) + 1;
        return n;
    }

    public static ArrayList<Integer> getOtherServerIds(int sId) {
        ArrayList<Integer> serverIds = new ArrayList<Integer>();
        for (int i = 1; i <= 3; i++) {
            if (i != sId) {
                serverIds.add(i);
            }
        }
        return serverIds;
    }

    public static ArrayList<Integer> getOtherServerIds() {
        ArrayList<Integer> serverIds = new ArrayList<Integer>();
        for (int i = 1; i <= 3; i++) {
            if (i != serverId) {
                serverIds.add(i);
            }
        }
        return serverIds;
    }

    public static ArrayList<Integer> getOServerIds(int sId) {
        ArrayList<Integer> serverIds = new ArrayList<Integer>();
        for (int i = 1; i <= 3; i++) {
            if (i != sId) {
                serverIds.add(i);
            }
        }
        return serverIds;
    }

    public static Map<String, String> getServerConnectionParams(int serverId) {
        Map<String, String> connParams = new HashMap<String, String>();
        if (serverId == 1) {
            String storageConnectionString = "DefaultEndpointsProtocol=http;"
                    + "AccountName=portalvhdsh8ghz0s9b7mx9;"
                    + "AccountKey=ThVIUXcwpsYqcx08mtIhRf6+XxvEuimK35/M65X+XlkdVCQNl4ViUiB/+tz/nq+eeZAEZCNrmFVQwwN3QiykvA==";
            String dbConnectionString = "jdbc:sqlserver://jvaakzlcvo.database.windows.net:1433"
                    + ";"
                    + "database=db_like"
                    + ";"
                    + "user=yanki@jvaakzlcvo" + ";" + "password=almeta%6y";
            connParams.put("storageConnectionString", storageConnectionString);
            connParams.put("dbConnectionString", dbConnectionString);
            connParams.put("serverIP", "138.91.113.97");
            connParams.put("url",
                    "https://portalvhdsh8ghz0s9b7mx9.blob.core.windows.net/");
        }

        if (serverId == 2) {

            String storageConnectionString = "DefaultEndpointsProtocol=http;"
                    + "AccountName=portalvhds27bmmb28df76b;"
                    + "AccountKey=+/R+aDSrb9BDH+HuU2eciV2fP4l6C1timdwao5czZrLE2vML3lt0omtSJhsL4NNi7rmTDWEflNlYXaeh0k+IZw==";
            String dbConnectionString = "jdbc:sqlserver://e55t52o9fy.database.windows.net:1433"
                    + ";"
                    + "database=db_like"
                    + ";"
                    + "user=db2@e55t52o9fy" + ";" + "password=NSyncgroup5";
            connParams.put("storageConnectionString", storageConnectionString);
            connParams.put("dbConnectionString", dbConnectionString);
            connParams.put("serverIP", "137.135.56.127");
            connParams.put("url",
                    "https://portalvhds27bmmb28df76b.blob.core.windows.net/");

        }

        if (serverId == 3) {
            String storageConnectionString = "DefaultEndpointsProtocol=http;"
                    + "AccountName=portalvhdsw81h4xbpp76b4;"
                    + "AccountKey=lBxPw8kuFh2lpRHpaSi3/bV/maTE8WJ7KSVJxU0W8/JltYqsm8W+k9qA1Vz38tyiBYX3p0a56Mid/sV88gzFrQ==";
            String dbConnectionString = "jdbc:sqlserver://ah0sncq8yf.database.windows.net:1433"
                    + ";"
                    + "database=db_like"
                    + ";"
                    + "user=MySQLAdmin@ah0sncq8yf" + ";" + "password=almeta%6y";
            connParams.put("storageConnectionString", storageConnectionString);
            connParams.put("dbConnectionString", dbConnectionString);
            connParams.put("serverIP", "137.135.57.215");
            connParams.put("url",
                    "https://portalvhdsw81h4xbpp76b4.blob.core.windows.net/");
        }
        return connParams;
    }

    public static String getStorageConnectionString() {
        Map<String, String> connParams = getServerConnectionParams(serverId);
        return connParams.get("storageConnectionString");
    }

    public static String getDBConnectionString() {
        Map<String, String> connParams = getServerConnectionParams(serverId);
        return connParams.get("dbConnectionString");
    }

    public static String getURL() {
        Map<String, String> connParams = getServerConnectionParams(serverId);
        System.out.println("the url is " + connParams.get("url"));
        return connParams.get("url");
    }

    public static boolean isServerUp() {
        Map<String, String> connParams = getServerConnectionParams(serverId);
        System.out.println(connParams.get("serverIP"));
        System.setProperty("javax.net.ssl.keyStore",
                System.getProperty("user.dir")
                + "\\src\\Settings\\clientkeystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "justdoit525");
        System.setProperty("javax.net.ssl.trustStore",
                System.getProperty("user.dir")
                + "\\src\\Settings\\clienttruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "justdoit525");
        System.setProperty("javax.net.ssl.debug", "all");

        try {
            System.setProperty("java.rmi.server.hostname",
                    connParams.get("serverIP"));
            Registry registry = LocateRegistry.getRegistry(
                    connParams.get("serverIP"), serverPort,
                    new SslRMIClientSocketFactory());
            server = (NsyncServerInterface) registry
                    .lookup("ServerInterfaceImpl");
            server.isUp();
            return true;
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
            serverId = getOtherServerIds().get(0);
            connParams = getServerConnectionParams(serverId);
            try {
                System.setProperty("java.rmi.server.hostname",
                        connParams.get("serverIP"));
                Registry registry = LocateRegistry.getRegistry(
                        connParams.get("serverIP"), serverPort,
                        new SslRMIClientSocketFactory());
                server = (NsyncServerInterface) registry
                        .lookup("ServerInterfaceImpl");
                server.isUp();
                return true;
            } catch (NotBoundException | RemoteException e2) {
                e2.printStackTrace();
                serverId = getOtherServerIds().get(1);
                connParams = getServerConnectionParams(serverId);
                try {
                    System.setProperty("java.rmi.server.hostname",
                            connParams.get("serverIP"));
                    Registry registry = LocateRegistry.getRegistry(
                            connParams.get("serverIP"), serverPort,
                            new SslRMIClientSocketFactory());
                    server = (NsyncServerInterface) registry
                            .lookup("ServerInterfaceImpl");
                    server.isUp();
                    return true;
                } catch (NotBoundException | RemoteException e3) {
                    e3.printStackTrace();
                    return false;
                }
            }
        }
    }

    public static NsyncServerInterface isServerUp(int sId, int serverPort) {
        NsyncServerInterface server = null;
        Map<String, String> connParams = getServerConnectionParams(sId);
        System.out.println(connParams.get("serverIP"));
        System.setProperty("javax.net.ssl.keyStore",
                System.getProperty("user.dir")
                + "\\src\\Settings\\clientkeystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "justdoit525");
        System.setProperty("javax.net.ssl.trustStore",
                System.getProperty("user.dir")
                + "\\src\\Settings\\clienttruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "justdoit525");
        System.setProperty("javax.net.ssl.debug", "all");

        try {
            System.setProperty("java.rmi.server.hostname",
                    connParams.get("serverIP"));
            Registry registry = LocateRegistry.getRegistry(
                    connParams.get("serverIP"), serverPort,
                    new SslRMIClientSocketFactory());
            server = (NsyncServerInterface) registry
                    .lookup("ServerInterfaceImpl");
            server.isUp();
            return server;
        } catch (NotBoundException | RemoteException e) {
            return null;
        }
        // return false;
    }
}
