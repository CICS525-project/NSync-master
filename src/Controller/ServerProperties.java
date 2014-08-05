package Controller;

import com.microsoft.azure.storage.blob.CloudBlob;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ServerProperties {
	public static int serverId = 1;
	
	public static String queueName = "fileevents";
        
        public static String userQueueName = "userevents";
	
	public static Thread publisher;
	
	public static Thread subscriber;
        
        public static Map<String, Date> userQueues = new HashMap<String, Date>();
        
        public static Map<CloudBlob, Date> leasedBlobs = new HashMap<CloudBlob, Date>();
}
