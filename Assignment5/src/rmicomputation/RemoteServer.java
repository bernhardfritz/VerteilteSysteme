package rmicomputation;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteServer extends Remote {
	
	public Integer addition(int a, int b) throws RemoteException;
	
	public Integer subtraction(int a, int b) throws RemoteException;
	
	public Integer multiplication(int a, int b) throws RemoteException;
	
	public Integer lucas(int x) throws RemoteException;
	
	public <T> T executeTask(Task<T> task) throws RemoteException;
}