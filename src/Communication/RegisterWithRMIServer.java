package Communication;

import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class RegisterWithRMIServer {

    static Registry registry;

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "138.91.113.97");
            System.setProperty("javax.net.ssl.keyStore", "C:\\ssl\\serverkeystore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "justdoit525");
            System.setProperty("javax.net.ssl.trustStore", "C:\\ssl\\servertruststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "justdoit525");
           // System.setProperty("java.rmi.activation.port", "9006");

            registry = LocateRegistry.createRegistry(9005, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory(null, null, true));
            NsyncServerInterface obj = new NsyncServerInterfaceImpl();
            // Registry registry = LocateRegistry.getRegistry();
            registry.rebind("ServerInterfaceImpl", obj);
            System.out.println("Student server " + obj + " registered");
        } catch (RemoteException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }
}
