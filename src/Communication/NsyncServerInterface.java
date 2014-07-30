package Communication;
import Controller.SendObject;
import java.rmi.*;


public interface NsyncServerInterface extends Remote {
      
    public boolean getPermission(String queueName) throws RemoteException;
    
    public boolean isUp() throws RemoteException;
    
    public SendObject processSendObject(SendObject s) throws RemoteException;
    
}
