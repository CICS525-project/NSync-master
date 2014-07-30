package Communication;

import Controller.SendObject;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;


import com.microsoft.azure.storage.queue.CloudQueue;

public class NsyncServerInterfaceImpl extends UnicastRemoteObject implements
		NsyncServerInterface {

	private final int serverId = 1;

	private static final long serialVersionUID = 1L;

	protected NsyncServerInterfaceImpl() throws RemoteException {
		super();
                
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean getPermission(String username) throws RemoteException {
		ArrayList<Integer> oids = Connection.getOServerIds(serverId);
		for (Integer i : oids) {
			Connection.serverId = i;
			ArrayList<CloudQueue> queues = QueueManager
					.getListOfQueues(username);
			for (CloudQueue q : queues) {
				long size = getQueueSize(q.getName(), i);
				if (size > 0) {
                                        System.out.println("Checking queue " + q.getName() + ". The size is " + size);
					return false;
				}
			}
		}
		return true;
	}

	private static long getQueueSize(String queuename, int serverId)
			throws RemoteException {
		Connection.serverId = serverId;
		long size = QueueManager.getQueueLength(queuename);
		return size;
	}

	@Override
	public boolean isUp() throws RemoteException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public SendObject processSendObject(SendObject s) throws RemoteException {
		
		//call the db update method to insert into the db
		//if insert is successfully done
            System.out.println("In the sendobject method");
		s.setEnteredIntoDB(true);		
		return s;
	}
	
	private boolean updateDB() {
		//Jasmine's method to update the online db
		return false;
	}
	
	

}
