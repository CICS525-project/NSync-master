package Communication;

import java.util.ArrayList;

import Controller.SendObject;
import Controller.ServerProperties;

public class PublishToOtherServers {

    public static void publisher(SendObject s) {
        String message = QueueManager.convertSendObjectToString(s);
        publishToServer(message);
    }

    public static void publisher(String username, String password, String email) {
        String message = username + "___" + password + "___" + email;
        publishToUserQueue(message);
    }

    private static void publishToUserQueue(String message) {
        ArrayList<Integer> oids = Connection
                .getOServerIds(ServerProperties.serverId);
        for (Integer i : oids) {
            String conn = Connection.getStorageConnectionString(i);
            QueueManager.enqueue(message, "userevents", conn);
        }
    }

    private static void publishToServer(String message) {
        ArrayList<Integer> oids = Connection
                .getOServerIds(ServerProperties.serverId);
        for (Integer i : oids) {
            String conn = Connection.getStorageConnectionString(i);
            QueueManager.enqueue(message, "fileevents", conn);
        }
    }
}
