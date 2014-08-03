package Communication;

import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class RegisterWithRMIServer {

    static Registry registry;

    public static void main(String[] args) {

        /* Accept connections from clients and servers */
        try {
            System.setProperty("java.rmi.server.hostname", "138.91.113.97");
            System.setProperty("javax.net.ssl.keyStore", "C:\\ssl\\serverkeystore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "justdoit525");
            System.setProperty("javax.net.ssl.trustStore", "C:\\ssl\\servertruststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "justdoit525");           

            registry = LocateRegistry.createRegistry(9005, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory(null, null, true));
            NsyncServerInterface obj = new NsyncServerInterfaceImpl();
            // Registry registry = LocateRegistry.getRegistry();
            registry.rebind("ServerInterfaceImpl", obj);
            System.out.println("Student server " + obj + " registered");

            /* start the old queues deleter */
            QueueManager.clearOldQueues();
            
            /*receive from files and user queues */
            Receiver.receiveFromQueue();
            Receiver.receiveFromUserQueue();

        } catch (RemoteException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }
}
