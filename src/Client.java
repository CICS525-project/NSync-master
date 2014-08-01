import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.rmi.ssl.SslRMIClientSocketFactory;

public class Client {

	public static void main(String[] args) throws NotBoundException {
		try {
			System.setProperty("java.rmi.server.hostname", "138.91.113.97");
			System.setProperty("javax.net.ssl.keyStore",
					"C:\\ssl\\clientkeystore.jks");
			System.setProperty("javax.net.ssl.keyStorePassword", "justdoit525");
			System.setProperty("javax.net.ssl.trustStore",
					"C:\\ssl\\clienttruststore.jks");
			System.setProperty("javax.net.ssl.trustStorePassword",
					"justdoit525");
			System.setProperty("javax.net.ssl.debug", "all");

			Registry registry = LocateRegistry.getRegistry("138.91.113.97",
					9006, new SslRMIClientSocketFactory());
			// Registry registry =
			// LocateRegistry.getRegistry(InetAddress.getLocalHost().getHostAddress().toString(),
			// 9005);
			ServerInterface student = (ServerInterface) registry
					.lookup("ServerInterfaceImpl");
			System.out.println("Server object " + student + " found");
			System.out.println("The score of student is "
					+ student.findScore("Ali"));

			// System.out.println("The method in the client class is " +
			// student.echoIP(new Client("Yanki")));

		} catch (RemoteException ex) {
			Logger.getLogger(Client.class.getName())
					.log(Level.SEVERE, null, ex);
		} // catch (UnknownHostException e) {
			// TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}
