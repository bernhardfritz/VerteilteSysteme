package loadbalancing;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;

public class Dispatcher implements RemoteDispatcher {

	private String name;

	private List<RemoteServer> serverStubList;
	
	public Dispatcher(String name) {
		this.serverStubList = new ArrayList<RemoteServer>();
		this.name = name;
	}
	
	@Override
	public void register(RemoteServer serverStub) {
		serverStubList.add(serverStub);
		try {
			System.out.println(serverStub.getName() + " has registered.");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public <T> Job<T> submit(UUID id, Callable<T> job, RemoteClient callback) throws RemoteException {
		System.out.println("\n" + callback.getName() + " trying to submit task(" + id + ").");
		
		Job<T> j = null;
		for (int i = 0; i < 3; i++) {
			RemoteServer selectedServerStub = serverStubList.get(new Random().nextInt(serverStubList.size()));
			System.out.println("Trying to submit task(" + id + ") to " + selectedServerStub.getName() + ".");
			j = selectedServerStub.submit(id, job, callback);
			if (j != null) {
				System.out.println("Job(" + id + ") from " + callback.getName() + " successfully submitted to " + selectedServerStub.getName() + ".");
				break;
			} else {
				System.out.println("Job(" + id + ") from " + callback.getName() + " rejected from " + selectedServerStub.getName() + ".");
			}
		}
		
		if (j == null) {
			System.out.println("Job(" + id + ") from " + callback.getName() + " rejected.");
		}
		
		return j;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public static void main(String args[]) {
		try {
			Dispatcher dispatcher = new Dispatcher((args.length > 0) ? args[0] : "Dispatcher"+new Random().nextInt(1000));
			RemoteDispatcher dispatcherStub = (RemoteDispatcher) UnicastRemoteObject.exportObject(dispatcher, 0);
			
		    Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
		    registry.rebind("Dispatcher", dispatcherStub);
		    
		    System.out.println(dispatcher.getName() + " started! Waiting for incoming requests...\n");
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
}