import java.rmi.*;

/**
 *
 * @author welcome
 */
public interface ServerInterface extends Remote {
    public double findScore(String name) throws RemoteException;
    
    public String echoIP(ClientInterface client) throws RemoteException;
    
    //public String copyBlob() throws RemoteException;
    
}
