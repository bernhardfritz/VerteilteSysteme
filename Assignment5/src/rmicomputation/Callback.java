package rmicomputation;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Callback extends Remote {

	public void answer(String answer) throws RemoteException;
	
	public boolean isShutdown() throws RemoteException;

	public void setShutdown(boolean shutdown) throws RemoteException;
	
	public String getAnswer() throws RemoteException;
}