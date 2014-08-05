package Communication;

import Controller.LeaseParams;
import Controller.SendObject;
import Controller.ServerProperties;
import com.microsoft.azure.storage.queue.CloudQueue;
import java.util.ArrayList;

public class PublishToOtherServers {

    public static void publisher(SendObject s, String queuename, LeaseParams p) {
        //send the sendobject + the server that it is coming from
        String message = QueueManager.convertSendObjectToString(s) + "___" + ServerProperties.serverId;
        System.out.println("Publishing to other clients on the same server and to other servers " + message);
        publishToOtherClientsOnSameServer(s, queuename, p.getServer1Lease());
        publishToServer(message, p);
    }

    public static void publishToOtherClientsOnSameServer(SendObject s, String queuename, String leaseID) {
        for (CloudQueue c : QueueManager.getListOfQueues(s.getUserID())) {
            if (!c.getName().equals(queuename)) {
                QueueManager.enqueue(QueueManager.convertSendObjectToString(s), c.getName());
            }
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

    private static void publishToServer(String message, LeaseParams p) {
        String leaseID = null;
        ArrayList<Integer> oids = Connection
                .getOServerIds(ServerProperties.serverId);
        for (Integer i : oids) {
            if(i == 1) {
                leaseID = p.getServer1Lease();
            } else if (i == 2) {
                leaseID = p.getServer2Lease();
            } else {
                leaseID = p.getServer3Lease();
            }
            String conn = Connection.getStorageConnectionString(i);
            QueueManager.enqueue(message + "___" + leaseID, "fileevents", conn);
        }
    }
}
