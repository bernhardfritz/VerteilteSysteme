package rmi;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements RemoteServer {
	
	private final String SHUTDOWN_PWD = "now";
	
	@Override
	public BigInteger addition(BigInteger a, BigInteger b) {
		return a.add(b);
	}
	
	@Override
	public boolean shutDown(String pwd) throws RemoteException {
		if (SHUTDOWN_PWD.equals(pwd)) {
			UnicastRemoteObject.unexportObject(this, true);
			return true;
		}
		return false;
	}
	
	public static void main(String args[]) {
		try {
			// create and export a server for remote method calls
		    Server server = new Server();
		    RemoteServer serverStub = (RemoteServer) UnicastRemoteObject.exportObject(server, 0);

		    // start the rmiregistry and bind the serverStub
		    Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
		    registry.rebind("Server", serverStub);
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
}