package Communication;
import java.rmi.*;

import Controller.SendObject;

public interface NsyncServerInterface extends Remote {      
	 
    public boolean getPermission(String queueName) throws RemoteException;
    
    public SendObject processSendObject(SendObject s) throws RemoteException;
    
    public boolean isUp() throws RemoteException;
    
    public boolean createAccount(String username, String password, String email) throws RemoteException;
    
    public boolean loginUser(String username, String password) throws RemoteException;
    
    public String createQueue(String username) throws RemoteException;  
    
    public boolean verifyUser(String username, String password) throws RemoteException;
    
    public String getGeneratedPassword(String password) throws RemoteException;
}
