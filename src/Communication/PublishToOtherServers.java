package Communication;

import java.util.ArrayList;

import Controller.SendObject;
import Controller.ServerProperties;

public class PublishToOtherServers {

	public static void publisher(SendObject s) {
		String message = QueueManager.convertSendObjectToString(s);		
		publishToServer(message);
	}

	private static void publishToServer(String message) {
		ArrayList<Integer> oids = Connection
				.getOServerIds(ServerProperties.serverId);		
		for (Integer i : oids) {
			Connection.serverId = 1;
			QueueManager.enqueue(message, "server" + String.valueOf(i));
		}
	}

}
