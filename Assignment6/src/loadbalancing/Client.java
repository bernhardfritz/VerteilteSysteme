package loadbalancing;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Client implements RemoteClient {
	
	private String name;
	
	private Map<UUID, Job<?>> jobs;
	
	public Client(String name) {
		this.jobs = new HashMap<UUID, Job<?>>();
		this.name = name;
	}

	@Override
	public <T> void callback(T t, UUID id) throws RemoteException {
		jobs.get(id).setResult(t);
	}
	
	@Override
	public String getName() throws RemoteException {
		return name;
	}
	
	public void putJob(UUID id, Job<?> job) {
		jobs.put(id, job);
	}
	
	public Map<UUID, Job<?>> getJobs() {
		return jobs;
	}
	
	public static void main(String[] args) {
		Client client = new Client((args.length > 0) ? args[0] : "Client"+new Random().nextInt(1000));
		try {
			Registry registry = LocateRegistry.getRegistry();
		    RemoteDispatcher dispatcherStub = (RemoteDispatcher) registry.lookup("Dispatcher");
			
		    RemoteClient clientStub = (RemoteClient) UnicastRemoteObject.exportObject(client, 0);
		    
		    for (int i = 1; i <= 40; i++) {
		    	UUID id = UUID.randomUUID();
			    Job<Integer> job = dispatcherStub.submit(id, new FibonacciCallable(i), clientStub);
			    if (job != null) {
			    	client.putJob(id, job);
			    } else {
			    	System.out.println("Job(" + id + ") rejected!");
			    }
		    }

		    while (!client.getJobs().isEmpty()) {
			    Iterator<UUID> iterator = client.getJobs().keySet().iterator();
			    while (iterator.hasNext()) {
			    	UUID id = iterator.next();
			    	Job<?> j = client.getJobs().get(id);
			    	if (j.isDone()) {
			    		System.out.println("Result(" + id + "): " + j.getResult());
			    		iterator.remove();
			    	}
			    }
		    }
		    
		    UnicastRemoteObject.unexportObject(client, true);
		    
		} catch (ConnectException e) {
		    System.err.println("Dispatcher has to be started first!");
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
}