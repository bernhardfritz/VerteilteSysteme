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
//		System.out.printf("Addition(%d, %d)\n", a, b);
		return a.add(b);
	}
	
	@Override
	public boolean shutDown(String pwd) throws RemoteException {
//		System.out.printf("ShutDown(\"%s\")\n", pwd);
		if (SHUTDOWN_PWD.equals(pwd)) {
			// unexport the server object to shut down gracefully
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
		    
//		    System.out.println("Server started! Waiting for incoming requests...");
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
}