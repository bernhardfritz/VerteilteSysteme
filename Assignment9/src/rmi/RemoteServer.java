package rmi;

import java.math.BigInteger;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteServer extends Remote {
	
	public BigInteger addition(BigInteger a, BigInteger b) throws RemoteException;
	
	public boolean shutDown(String pwd) throws RemoteException;
}