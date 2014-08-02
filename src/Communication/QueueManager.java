package Communication;

import java.util.ArrayList;
import java.util.Date;

import Controller.SendObject;
import Controller.SendObject.EventType;
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.queue.*;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public class QueueManager {

    public static final String storageConnectionString = Connection
            .getStorageConnectionString();

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
            // Output the stack trace.
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
        return retrievedMessage.toString();
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

            // Display the queue length.
            System.out.println(String.format("Queue length: %d",
                    cachedMessageCount));
        } catch (Exception e) {
            // Output the stack trace.
            e.printStackTrace();
        }
        return cachedMessageCount;
    }

    public static void main(String[] args) {
		// System.out.println(User.getUsername());
        //getListOfQueues("democontainer");
        //enqueue("Yanki don kolo", "democontainer1406268314962");
        //getQueueLength("democontainer1406268314962");
        //deque("democontainer1406268314962");
        //getQueueLength("democontainer1406268314962");

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

        s = o.getFileName() + "|" + o.getNewFileName() + "|" + o.getFilePath()
                + "|" + o.getEvent().toString() + "|" + enteredInDB + "|"
                + o.getTimeStamp().toString() + "|" + isAFolder + "|"
                + o.getHash() + "|" + o.getID() + "|" + o.getUserID();
        return s;
    }

    public static SendObject convertStringToSendObject(String s) {
        SendObject o = new SendObject();
        String[] parts = s.split("|");
        o.setFileName(parts[0]);
        o.setNewFileName(parts[1]);
        o.setFilePath(parts[2]);
        o.setEvent(getThisEventType(parts[3]));
        o.setEnteredIntoDB(check(parts[4]));
        o.setTimeStamp(new Date(parts[5]));
        o.setIsAFolder(check(parts[6]));
        //o.setHash();
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
