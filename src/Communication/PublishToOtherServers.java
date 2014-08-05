package Communication;

import Controller.SendObject;
import Controller.ServerProperties;
import com.microsoft.azure.storage.queue.CloudQueue;
import java.util.ArrayList;

public class PublishToOtherServers {

    public static void publisher(SendObject s) {
        //send the sendobject + the server that it is coming from
        String message = QueueManager.convertSendObjectToString(s) + "___" + ServerProperties.serverId;
        System.out.println("Publishing to other clients on the same server and to other servers " + message);
        publishToOtherClientsOnSameServer(s);
        publishToServer(message);
    }
    
    public static void publishToOtherClientsOnSameServer(SendObject s) {
        for(CloudQueue c:QueueManager.getListOfQueues(s.getUserID())) {
            QueueManager.enqueue(QueueManager.convertSendObjectToString(s), c.getName());
        }
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
