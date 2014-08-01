package Communication;

import java.util.ArrayList;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import Controller.SendObject;
import Controller.ServerProperties;

public class Receiver {
	public static void receiveFromQueue() {

		// get the queue of the client
		ServerProperties.subscriber = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					Connection.serverId = 1;
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
}