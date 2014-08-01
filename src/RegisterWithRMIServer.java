import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.registry.*;

public class RegisterWithRMIServer {
 public static void main(String[] args) {
	 
	 ClassLoader cl = ClassLoader.getSystemClassLoader();
	 
     URL[] urls = ((URLClassLoader)cl).getURLs();

     for(URL url: urls){
     	System.out.println(url.getFile());
     }
     
	 try {
		// System.setProperty("java.rmi.server.hostname", "24.86.28.122");
         System.setProperty("java.rmi.activation.port", "9005");
		 Registry registry = LocateRegistry.createRegistry(9005);
		 ServerInterface obj = new ServerInterfaceImpl();
		 
		// Registry registry = LocateRegistry.getRegistry();
		 registry.rebind("ServerInterfaceImpl", obj);
		 System.out.println("Student server " + obj + " registered");
	 } catch (Exception ex) {
		 ex.printStackTrace();
	 }
 }
}
