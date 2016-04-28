package loadbalancing;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;
import java.util.concurrent.Callable;

public interface RemoteServer extends Remote {
	
	public <T> Job<T> submit(UUID id, Callable<T> job, RemoteClient callback) throws RemoteException;
}