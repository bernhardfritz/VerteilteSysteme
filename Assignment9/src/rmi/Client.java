package rmi;

import java.math.BigInteger;
import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

public class Client {
	
	public static void main(String[] args) {
		try {
			 // get registry and create serverStub from registry lookup
			Registry registry = LocateRegistry.getRegistry();
		    RemoteServer serverStub = (RemoteServer) registry.lookup("Server");
		    
		    Random r = new Random(0);
			long start = System.currentTimeMillis();
			for(int i = 0; i < 1000; i++) {
				serverStub.addition(BigInteger.valueOf(r.nextLong()), BigInteger.valueOf(r.nextLong()));
			}
			long end = System.currentTimeMillis();
			double duration = (end - start) / 1000.0; // in ms
			System.out.println("[RMI] Duration: " + duration + " ms");
		    
		    serverStub.shutDown("now");
		} catch (ConnectException e) {
		    System.err.println("Server has to be started first!");
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
}