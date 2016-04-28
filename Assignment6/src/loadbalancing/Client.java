package loadbalancing;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class Client implements RemoteClient {
	
	private Map<UUID, Job<?>> jobs;
	
	public Client() {
		this.jobs = new HashMap<UUID, Job<?>>();
	}

	@Override
	public <T> void callback(T t, UUID id) throws RemoteException {
		jobs.get(id).setResult(t);
	}
	
	public void putJob(UUID id, Job<?> job) {
		jobs.put(id, job);
	}
	
	public Map<UUID, Job<?>> getJobs() {
		return jobs;
	}
	
	public static void main(String[] args) {
		Client client = new Client();
		try {
			 // get registry and create serverStub from registry lookup
			Registry registry = LocateRegistry.getRegistry();
		    RemoteServer serverStub = (RemoteServer) registry.lookup("Server");
			
			// create a callbackClient for deepThought execution
		    RemoteClient clientStub = (RemoteClient) UnicastRemoteObject.exportObject(client, 0);
		    
		    for (int i = 1; i <= 30; i++) {
		    	UUID id = UUID.randomUUID();
			    Job<Integer> job = serverStub.submit(id, new FibonacciCallable(i), clientStub);
			    if (job != null) {
			    	client.putJob(id, job);
			    }
		    }
		    
		    Iterator<UUID> iterator = client.getJobs().keySet().iterator();
		    while (iterator.hasNext()) {
		    	UUID id = iterator.next();
		    	Job<?> j = client.getJobs().get(id);
		    	if (j.isDone()) {
		    		System.out.println("Result(" + id + "): " + j.getResult());
		    		iterator.remove();
		    	}
		    }
		    
		    // unexport callbackClient to shut down gracefully
		    UnicastRemoteObject.unexportObject(client, true);
		    
		} catch (ConnectException e) {
		    System.err.println("Server has to be started first!");
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
}