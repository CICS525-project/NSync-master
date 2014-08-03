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

    public static void processMessageFromQueue(SendObject s) {
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
                        String[] comps = message.split("|");
                        if(!comps[0].equals("") && comps[1].equals("") && comps[2].equals("")) {
                            BlobManager.createContainter(comps[0]);
                            UserManager.createUser(comps[0], comps[1], comps[2]);
                        } 
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

}
