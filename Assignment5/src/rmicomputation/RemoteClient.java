package rmicomputation;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteClient extends Remote {
	public void callback(String s) throws RemoteException;
}