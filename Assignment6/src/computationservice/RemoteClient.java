package computationservice;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface RemoteClient extends Remote {
	public <T> void callback(T t, UUID id) throws RemoteException;
}