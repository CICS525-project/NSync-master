package Communication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;

import Controller.SendObject;
import ServerDBManager.UserManager;

import com.microsoft.azure.storage.queue.CloudQueue;

public class NsyncServerInterfaceImpl extends UnicastRemoteObject implements
        NsyncServerInterface {

    private int serverId = 1;

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
        return false;
    }

    @Override
    public SendObject processSendObject(SendObject s) throws RemoteException {

		// call the db update method to insert into the db
        // if insert is successfully done
        s.setEnteredIntoDB(true);
        return s;
    }

    private boolean updateDB() {
        // Jasmine's method to update the online db
        return false;
    }

    @Override
    public boolean createAccount(String username, String password, String email)
            throws RemoteException {
        if (UserManager.createUser(username, password, email)) {
            BlobManager.createContainter(username);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean loginUser(String username, String password)
            throws RemoteException {
        if (UserManager.loginUser(username, password)) {
            return true;
        }
        return false;
    }

    @Override
    public String createQueue(String username) throws RemoteException {
        // TODO Auto-generated method stub
        String queuename = username + new Date().getTime();
        QueueManager.createQueue(queuename);
        return queuename;
    }

    @Override
    public boolean verifyUser(String username, String password)
            throws RemoteException {
        // TODO Auto-generated method stub
        if (UserManager.verifyUser(username, password)) {
            return true;
        }
        return false;
    }

    @Override
    public String getGeneratedPassword(String password) throws RemoteException {
        return UserManager.getGeneratedPassword(password);
    }

}
