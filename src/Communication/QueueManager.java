package Communication;

import Controller.SendObject;
import Controller.ServerProperties;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.LeaseState;
import com.microsoft.azure.storage.queue.*;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueueManager {

    public static final String storageConnectionString = Connection
            .getStorageConnectionString(ServerProperties.serverId);

    public static void createQueue(String queueName) {
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount = CloudStorageAccount
                    .parse(storageConnectionString);

            // Create the queue client.
            CloudQueueClient queueClient = storageAccount
                    .createCloudQueueClient();

            // Retrieve a reference to a queue.
            CloudQueue queue = queueClient.getQueueReference(queueName);

            // Create the queue if it doesn't already exist.
            queue.createIfNotExists();
        } catch (Exception e) {
            // Output the stack trace.
            e.printStackTrace();
        }
    }

    public static boolean queueExists(String queueName) {
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount = CloudStorageAccount
                    .parse(storageConnectionString);

            // Create the queue client.
            CloudQueueClient queueClient = storageAccount
                    .createCloudQueueClient();

            // Retrieve a reference to a queue.
            CloudQueue queue = queueClient.getQueueReference(queueName);
            return true;
        } catch (URISyntaxException | InvalidKeyException | StorageException e) {
            // Output the stack trace. Means the queue does not exist
            e.printStackTrace();
            return false;
        }
    }

    public static void enqueue(String mes, String queueName) {
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount = CloudStorageAccount
                    .parse(storageConnectionString);

            // Create the queue client.
            CloudQueueClient queueClient = storageAccount
                    .createCloudQueueClient();

            // Retrieve a reference to a queue.
            CloudQueue queue = queueClient.getQueueReference(queueName);

            // Create the queue if it doesn't already exist.
            queue.createIfNotExists();

            // Create a message and add it to the queue.
            CloudQueueMessage message = new CloudQueueMessage(mes);
            queue.addMessage(message);
        } catch (Exception e) {
            // Output the stack trace.
            e.printStackTrace();
        }
    }

    public static void enqueue(String mes, String queueName, String storageConnectionString) {
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount = CloudStorageAccount
                    .parse(storageConnectionString);

            // Create the queue client.
            CloudQueueClient queueClient = storageAccount
                    .createCloudQueueClient();

            // Retrieve a reference to a queue.
            CloudQueue queue = queueClient.getQueueReference(queueName);

            // Create the queue if it doesn't already exist.
            queue.createIfNotExists();

            // Create a message and add it to the queue.
            CloudQueueMessage message = new CloudQueueMessage(mes);
            queue.addMessage(message);
        } catch (Exception e) {
            // Output the stack trace.
            e.printStackTrace();
        }
    }

    public static String deque(String queueName) {
        CloudQueueMessage retrievedMessage = null;
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount = CloudStorageAccount
                    .parse(storageConnectionString);

            // Create the queue client.
            CloudQueueClient queueClient = storageAccount
                    .createCloudQueueClient();

            // Retrieve a reference to a queue.
            CloudQueue queue = queueClient.getQueueReference(queueName);

            // Retrieve the first visible message in the queue.
            retrievedMessage = queue.retrieveMessage(200,
                    null, null);

            // retrievedMessage.
            if (retrievedMessage != null) {
                // Process the message in less than 30 seconds, and then delete
                // the message.
                queue.deleteMessage(retrievedMessage);
            }
        } catch (Exception e) {
            // Output the stack trace.
            e.printStackTrace();
        }
        try {
            return retrievedMessage.getMessageContentAsString();
        } catch (StorageException ex) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "_________";
    }

    public static void deleteQueue(String queueName) {
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount = CloudStorageAccount
                    .parse(storageConnectionString);

            // Create the queue client.
            CloudQueueClient queueClient = storageAccount
                    .createCloudQueueClient();

            // Retrieve a reference to a queue.
            CloudQueue queue = queueClient.getQueueReference(queueName);

            // Delete the queue if it exists.
            queue.deleteIfExists();
        } catch (Exception e) {
            // Output the stack trace.
            e.printStackTrace();
        }
    }

    public static ArrayList<CloudQueue> getListOfQueues(String prefix) {
        ArrayList<CloudQueue> queues = new ArrayList<CloudQueue>();
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount = CloudStorageAccount
                    .parse(storageConnectionString);

            // Create the queue client.
            CloudQueueClient queueClient = storageAccount
                    .createCloudQueueClient();

            // Loop through the collection of queues.
            for (CloudQueue queue : queueClient.listQueues(prefix)) {
                // Output each queue name.
                System.out.println(queue.getName());
                queues.add(queue);
            }
        } catch (Exception e) {
            // Output the stack trace.
            e.printStackTrace();
        }
        return queues;
    }

    public static long getQueueLength(String queueName) {
        long cachedMessageCount = 0;
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount = CloudStorageAccount
                    .parse(storageConnectionString);

            // Create the queue client.
            CloudQueueClient queueClient = storageAccount
                    .createCloudQueueClient();

            // Retrieve a reference to a queue.
            CloudQueue queue = queueClient.getQueueReference(queueName);

            // Download the approximate message count from the server.
            queue.downloadAttributes();

            // Retrieve the newly cached approximate message count.
            cachedMessageCount = queue.getApproximateMessageCount();

        } catch (Exception e) {
            // Output the stack trace.
            e.printStackTrace();
        }
        return cachedMessageCount;
    }

    public static void clearOldQueues() {
        Thread t = new Thread(new Runnable() {

            private long timeDifferenceInHours(Date laterDate, Date earlierDate) {
                long result = ((laterDate.getTime() / (60000 * 60)) - (earlierDate.getTime() / (60000 * 60)));
                return result;
            }

            private long timeDifferenceInMinutes(Date laterDate, Date earlierDate) {
                long result = ((laterDate.getTime() / (60000)) - (earlierDate.getTime() / (60000)));
                return result;
            }

            @Override
            public void run() {
                while (true) {
                    for (CloudQueue cq : getListOfQueues("")) {
                        String queueName = cq.getName();
                        if (queueName.matches(".*\\d+.*")) {
                            long timeCreated = Long.valueOf(queueName.replaceAll("\\D+", ""));

                            if (timeDifferenceInMinutes(new Date(), new Date(timeCreated)) > 2 && !ServerProperties.userQueues.containsKey(queueName)) {
                                if (!queueName.equals("fileevents") && !queueName.equals("userevents")) {
                                    System.out.println("Deleting " + queueName);
                                    deleteQueue(queueName);
                                }
                            }
                        }
                    }

                    //unlock the blobs that have been locked for more than 5 minutes
                    for (Map.Entry<CloudBlob, Date> entry : ServerProperties.leasedBlobs.entrySet()) {
                        CloudBlob b = entry.getKey();
                        Date date = entry.getValue();

                        if (timeDifferenceInMinutes(new Date(), date) >= 5) {
                            try {
                                if (b.exists()) {
                                    b.downloadAttributes();
                                    if (b.getProperties().getLeaseState().equals(LeaseState.LEASED)) {
                                        b.breakLease(0);
                                        ServerProperties.leasedBlobs.remove(b);
                                    }
                                }
                            } catch (StorageException ex) {
                                Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }

                    // for(CloudBlob b: )
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        t.start();
    }

    public static void main(String[] args) {
        // System.out.println(User.getUsername());
        for (CloudQueue s : getListOfQueues("demo")) {
            try {
                s.delete();
            } catch (StorageException ex) {
                Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static String convertSendObjectToString(SendObject o) {
        String s;
        int enteredInDB = 0;
        if (o.isEnteredIntoDB()) {
            enteredInDB = 1;
        }

        int isAFolder = 0;
        if (o.isIsAFolder()) {
            isAFolder = 1;
        }

        s = o.getFileName() + "___" + o.getNewFileName() + "___" + o.getFilePath()
                + "___" + o.getEvent() + "___" + enteredInDB + "___"
                + o.getTimeStamp().toString() + "___" + isAFolder + "___"
                + o.getHash() + "___" + o.getID() + "___" + o.getUserID();
        return s;
    }

    public static SendObject convertStringToSendObject(String s) {
        SendObject o = new SendObject();
        String[] parts = s.split("___");
        o.setFileName(parts[0]);
        o.setNewFileName(parts[1]);
        o.setFilePath(parts[2]);
        o.setEvent(getThisEventType(parts[3]));
        o.setEnteredIntoDB(check(parts[4]));
        o.setTimeStamp(new Date(parts[5]));
        o.setIsAFolder(check(parts[6]));
        o.setHash(parts[7]);
        o.setID(parts[8]);
        o.setUserID(parts[9]);
        return o;
    }

    private static SendObject.EventType getThisEventType(String s) {
        if (s.equalsIgnoreCase("create")) {
            return SendObject.EventType.Create;
        } else if (s.equalsIgnoreCase("rename")) {
            return SendObject.EventType.Rename;
        } else if (s.equalsIgnoreCase("modify")) {
            return SendObject.EventType.Modify;
        } else if (s.equalsIgnoreCase("delete")) {
            return SendObject.EventType.Delete;
        } else {
            return null;
        }
    }

    private static boolean check(String s) {
        if (s.equals("0")) {
            return false;
        } else {
            return true;
        }
    }
}
