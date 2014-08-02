package Communication;

import Controller.SendObject;
import ServerDBManager.UserManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
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

    private static Map<String, Date> userQueues = new HashMap<String, Date>();

    protected NsyncServerInterfaceImpl() throws RemoteException {
        super();
        deleteOldQueues();
    }

    @Override
    public boolean getPermission(String queuename) throws RemoteException {
        String username = queuename.replaceAll("\\d", "");
        ArrayList<Integer> oids = Connection.getOServerIds(1);
        NsyncServerInterface server1 = Connection.isServerUp(oids.get(0), 9005);
        boolean server1P = server1.getPermissionForServers(queuename);

        NsyncServerInterface server2 = Connection.isServerUp(oids.get(1), 9005);
        boolean server2P = server2.getPermissionForServers(queuename);
        
        boolean self = getPermissionOnSelf(username, queuename);
        
        if(self && server1P && server2P) {
            return true;
        } else {
            return false;
        }      
    }

    public boolean getPermissionForServers(String username) {
        if (userQueues.size() > 0) {
            Iterator it = userQueues.entrySet().iterator();
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
        if (userQueues.size() > 0) {
            Iterator it = userQueues.entrySet().iterator();
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
        if (s.getEvent().equals(SendObject.EventType.Create) || s.getEvent().equals(SendObject.EventType.Create)) {
            //client should do the update
        }

        if (s.getEvent().equals(SendObject.EventType.Delete)) {
            //call blobmanager to delete the file from the blob
            BlobManager.deleteBlob(s.getFilePath() + "/" + s.getFileName());
        }

        if (s.getEvent().equals(SendObject.EventType.Delete)) {
            //call blobmanager to delete the file from the blob
            BlobManager.renameBlob(s.getFilePath() + "/" + s.getNewFileName(), s.getFilePath() + "/" + s.getFileName());
        }
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
            return queuename;
        } else {
            if (userQueues.containsKey(oldQueueName)) {
                return oldQueueName;
            } else {
                String queuename = username + new Date().getTime();
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
    public boolean serverToClientSync(Date lastTS, String qName) throws RemoteException {
        try {
            //assuming I get back an arraylist of sendobjects
            ArrayList<SendObject> toSync = new ArrayList<SendObject>(); //this should be Yasmin's method 
            for (SendObject s : toSync) {
                String message = QueueManager.convertSendObjectToString(s);
                QueueManager.enqueue(message, qName);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean maintainQueue(String queuename) throws RemoteException {
        //get the queuename from the list
        if (userQueues.containsKey(queuename)) {
            userQueues.put(queuename, new Date());
            return true;
        } else {
            userQueues.put(queuename, new Date());
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
                    if (userQueues.size() > 0) {
                        Iterator it = userQueues.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pairs = (Map.Entry) it.next();
                            System.out.println(pairs.getKey() + " = " + pairs.getValue());
                            if (timeDifferenceInMinutes(new Date(), new Date(pairs.getValue().toString())) >= 3) {
                                userQueues.remove(pairs.getKey().toString());
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
