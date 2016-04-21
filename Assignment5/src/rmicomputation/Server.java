package rmicomputation;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements RemoteServer {

	@Override
	public Integer addition(int a, int b) {
		return a + b;
	}

	@Override
	public Integer subtraction(int a, int b) {
		return a - b;
	}

	@Override
	public Integer multiplication(int a, int b) {
		return a * b;
	}

	@Override
	public Integer lucas(int x) {
		if(x == 0) return 2;
		if(x == 1) return 1;
		return lucas(x - 1) + lucas(x - 2);
	}
	
	public static void main(String args[]) {
		try {
		    Server obj = new Server();
		    RemoteServer stub = (RemoteServer) UnicastRemoteObject.exportObject(obj, 0);

		    Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
		    registry.rebind("Server", stub);
		    System.out.println("Server startet! Waiting for incoming requests...");
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
}