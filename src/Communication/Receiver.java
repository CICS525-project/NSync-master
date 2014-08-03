package Communication;

import java.util.ArrayList;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import Controller.SendObject;
import Controller.ServerProperties;
import ServerDBManager.UserManager;

public class Receiver {

    public static void receiveFromQueue() {
        QueueManager.createQueue(ServerProperties.queueName);
        // get the queue of the client
        ServerProperties.subscriber = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (QueueManager.getQueueLength(ServerProperties.queueName) > 0) {
                        // call to DBManager method to resolve conflicts missing
                        String message = QueueManager
                                .deque(ServerProperties.queueName);
                        SendObject d = QueueManager
                                .convertStringToSendObject(message);
                        int serverId = Integer.parseInt(message.substring(message.lastIndexOf("___")));
                        System.out.println("Received message from server " + serverId + " and the message is " + message);
                        actOnMessage(d, serverId);
                        processMessageFromQueue(d);
                        // call to dbManager to update the SendObject missing
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // continually check to see if the queue has something in a thread
        ServerProperties.subscriber.start();
    }

    private static void actOnMessage(SendObject s, int fromWhichServer) {
        if (s.getEvent().equals(SendObject.EventType.Create) || s.getEvent().equals(SendObject.EventType.Create)) {
            //client should do the update
            String path = s.getUserID() + "/" + pathParser(s.getFilePath()) + s.getFileName();
            BlobManager.copyBlob(s.getUserID(), s.getUserID(), path, fromWhichServer, ServerProperties.serverId);
        }

        if (s.getEvent().equals(SendObject.EventType.Delete)) {
            //call blobmanager to delete the file from the blob
            BlobManager.deleteBlob(s.getUserID() + "/" + pathParser(s.getFilePath()) + s.getFileName());
        }

        if (s.getEvent().equals(SendObject.EventType.Delete)) {
            //call blobmanager to delete the file from the blob
            BlobManager.renameBlob(s.getUserID() + "/" + pathParser(s.getFilePath()) + s.getNewFileName(), s.getUserID() + "/" + pathParser(s.getFilePath()) + s.getFileName());
        }
    }

    private static void processMessageFromQueue(SendObject s) {
        String prefix = s.getUserID();
        ArrayList<CloudQueue> qs = QueueManager.getListOfQueues(prefix);
        for (CloudQueue cq : qs) {
            try {
                cq.addMessage(new CloudQueueMessage(QueueManager
                        .convertSendObjectToString(s)));
            } catch (StorageException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public static void receiveFromUserQueue() {

        QueueManager.createQueue(ServerProperties.userQueueName);
        // get the queue of the client
        Thread u = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (QueueManager.getQueueLength(ServerProperties.userQueueName) > 0) {

                        String message = QueueManager
                                .deque(ServerProperties.userQueueName);
                        String[] comps = message.split("___");
                        System.out.println("Message received is " + message);
                        System.out.println(comps[0] + " " + comps[1] + " " + comps[2] + " and length is " + comps.length);
                        BlobManager.createContainter(comps[0]);
                        UserManager.createUserFromQueue(comps[0], comps[1], comps[2]);
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // continually check to see if the queue has something in a thread
        u.start();
    }

    public static String pathParser(String path) {
        if (path == null || path.equals("")) {
            return "";
        } else {
            return path + "/";
        }
    }

}
