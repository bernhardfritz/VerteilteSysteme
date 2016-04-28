package loadbalancing;

import java.rmi.RemoteException;

public interface RemoteDispatcher extends RemoteServer {

	public void register(RemoteServer server) throws RemoteException;
}