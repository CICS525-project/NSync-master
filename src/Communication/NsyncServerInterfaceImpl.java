package Communication;

import Controller.SendObject;
import Controller.ServerProperties;
import ServerDBManager.ServerDBManager;
import ServerDBManager.UserManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NsyncServerInterfaceImpl extends UnicastRemoteObject implements
        NsyncServerInterface {

    private int serverId = 1;

    private static final long serialVersionUID = 1L;

    protected NsyncServerInterfaceImpl() throws RemoteException {
        super();
        deleteOldQueues();
    }

    @Override
    public boolean getPermission(SendObject s) throws RemoteException {
        
        String blobName = Receiver.pathParser(s.getFilePath()) + s.getFileName();
        String containerName = s.getUserID();
        
        String server1Lease = BlobManager.acquireLease(blobName, containerName, 1);
        String server2Lease = BlobManager.acquireLease(blobName, containerName, 2);
        String server3Lease = BlobManager.acquireLease(blobName, containerName, 3);
        
        if(server1Lease == null || server2Lease == null || server3Lease == null) {
            return false;
        }        
        return true;
    }

    public boolean getPermissionForServers(String username) {
        if (ServerProperties.userQueues.size() > 0) {
            Iterator it = ServerProperties.userQueues.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                System.out.println(pairs.getKey() + " = " + pairs.getValue());
                if (pairs.getValue().toString().startsWith(username)) {
                    return false;
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
            return true;
        } else {
            return true;
        }
    }

    private boolean getPermissionOnSelf(String username, String queueName) {
        if (ServerProperties.userQueues.size() > 0) {
            Iterator it = ServerProperties.userQueues.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                System.out.println(pairs.getKey() + " = " + pairs.getValue());
                if (pairs.getValue().toString().startsWith(username) && !pairs.getValue().toString().equals(queueName)) {
                    return false;
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
            return true;
        } else {
            return true;
        }
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
    public SendObject serverDBUpdate(SendObject s) throws RemoteException {

        // call the db update method to insert into the db
        // if insert is successfully done
        // check the value of the event
        
        System.out.println("The path from the sendobject is " + s.getUserID() + "/" + Receiver.pathParser(s.getFilePath()) + s.getFileName());
        
        if (s.getEvent().equals(SendObject.EventType.Create) || s.getEvent().equals(SendObject.EventType.Modify)) {
            //client should do the update
        }

        if (s.getEvent().equals(SendObject.EventType.Delete)) {
            //call blobmanager to delete the file from the blob
            BlobManager.deleteBlob(s.getUserID() + "/" + Receiver.pathParser(s.getFilePath()) + s.getFileName());
        }

        if (s.getEvent().equals(SendObject.EventType.Rename)) {
            //call blobmanager to delete the file from the blob
            BlobManager.renameBlob(s.getUserID() + "/" + Receiver.pathParser(s.getFilePath()) + s.getNewFileName(), s.getUserID() + "/" + Receiver.pathParser(s.getFilePath()) + s.getFileName());
        }

        //broadcast to other servers
        PublishToOtherServers.publisher(s);
        //return the sendObject to the client;
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

            //broadcast to other servers
            PublishToOtherServers.publisher(username, password, email);
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
    public String createQueue(String username, String oldQueueName) throws RemoteException {
        if (oldQueueName.equals("")) {
            String queuename = username + new Date().getTime();
            QueueManager.createQueue(queuename);
            ServerProperties.userQueues.put(queuename, new Date());
            return queuename;
        } else {
            if (ServerProperties.userQueues.containsKey(oldQueueName) && QueueManager.queueExists(oldQueueName)) {
                return oldQueueName;
            } else {
                String queuename = username + new Date().getTime();
                ServerProperties.userQueues.put(queuename, new Date());
                QueueManager.createQueue(queuename);
                return queuename + "-";
            }
        }
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

    @Override
    public void serverToClientSync(Timestamp lastTS, String qName) throws RemoteException {
       /* try {
            //assuming I get back an arraylist of sendobjects
            ArrayList<SendObject> toSync = new ArrayList<SendObject>(); //this should be Yasmin's method 
            for (SendObject s : toSync) {
                String message = QueueManager.convertSendObjectToString(s);
                QueueManager.enqueue(message, qName);
            }
            return true;
        } catch (Exception e) {
            return false;
        } */
        ServerDBManager.DBServerToClientList(qName.replaceAll("\\d",""), lastTS, qName);
    }

    @Override
    public boolean maintainQueue(String queuename) throws RemoteException {
        //get the queuename from the list
        if (ServerProperties.userQueues.containsKey(queuename)) {
            ServerProperties.userQueues.put(queuename, new Date());
            return true;
        } else {
            ServerProperties.userQueues.put(queuename, new Date());
            return false;
        }
    }

    private void deleteOldQueues() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(NsyncServerInterfaceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (ServerProperties.userQueues.size() > 0) {
                        Iterator it = ServerProperties.userQueues.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pairs = (Map.Entry) it.next();
                            System.out.println(pairs.getKey() + " = " + pairs.getValue());
                            if (timeDifferenceInMinutes(new Date(), new Date(pairs.getValue().toString())) >= 3) {
                                ServerProperties.userQueues.remove(pairs.getKey().toString());
                                QueueManager.deleteQueue(pairs.getKey().toString());
                            }
                            it.remove(); // avoids a ConcurrentModificationException
                        }
                    }
                }
            }
        });
        t.start();
    }

    private long timeDifferenceInMinutes(Date laterDate, Date earlierDate) {
        long result = ((laterDate.getTime() / 60000) - (earlierDate.getTime() / 60000));
        return result;
    }

}
