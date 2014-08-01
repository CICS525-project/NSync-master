import java.rmi.*;
import java.rmi.server.*;
import java.util.HashMap;

public class ServerInterfaceImpl extends UnicastRemoteObject implements ServerInterface {
	
	private HashMap<String, Double> scores = new HashMap<String, Double>();
    private volatile ClientInterface client;
	
    public ServerInterfaceImpl() throws RemoteException {
		//super();
		// TODO Auto-generated constructor stub
    	initializeStudent();
	}
    
    protected void initializeStudent() {
    	scores.put("John", new Double(90.5));
    	scores.put("Michael", new Double(100));
    	scores.put("Ali", new Double(98.5));
    }

	public double findScore(String name) throws RemoteException {
        //throw new UnsupportedOperationException("Not supported yet."); 
        Double d = (Double) scores.get(name);
        
        if(d==null) {
        	System.out.println("Student " + name + " was not found");
        	return -1;
        } else {
        	System.out.println("Student " + name + " score is " + d.doubleValue());
        	return d.doubleValue();
        }   
        
        
    }
	
	public String echoIP(ClientInterface client) {
		this.client = client;
		//System.out.println(client.name);
		try {
			System.out.println("The score on the client is " + client.findScore());
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String remoteClient = null;
		try {
			remoteClient = RemoteServer.getClientHost().toString();
			
		} catch (ServerNotActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return remoteClient;
	}

	public String copyBlob() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
}
