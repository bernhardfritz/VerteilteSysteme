package rmicomputation;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements RemoteServer {
	
	private final String SHUTDOWN_PWD = "now";
	
	@Override
	public Integer addition(int a, int b) {
		System.out.printf("Addition(%d, %d)\n", a, b);
		return a + b;
	}

	@Override
	public Integer subtraction(int a, int b) {
		System.out.printf("Subtraction(%d, %d)\n", a, b);
		return a - b;
	}

	@Override
	public Integer multiplication(int a, int b) {
		System.out.printf("Multiplication(%d, %d)\n", a, b);
		return a * b;
	}

	@Override
	public Integer lucas(int n) {
		System.out.printf("Lucas(%d)\n", n);
		return calcLucasNumbers(n);
	}
	
	private Integer calcLucasNumbers(int n) {
		if(n == 0) return 2;
		if(n == 1) return 1;
		return calcLucasNumbers(n - 1) + calcLucasNumbers(n - 2);
	}
	
	@Override
	public <T> T executeTask(Task<T> task) throws RemoteException {
		System.out.printf("ExecuteTask(%s)\n", task.getClass().getSimpleName());
		return task.execute();
	}
	
	@Override
	public void deepThought(final String questionParam, final RemoteClient clientStub) throws RemoteException {
		System.out.printf("DeepThought(\"%s\")\n", questionParam);
		
		// create new thread for task execution
		new Thread() {
			private String question;
			private RemoteClient callback;
			
			public void run() {
				// set question and callback
				question = questionParam;
				callback = clientStub;

				// sleep...
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				try {
					// return answer
					callback.callback("The answer to your question \"" + question + "\" is probably 42.");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	@Override
	public boolean shutDown(String pwd) throws RemoteException {
		System.out.printf("ShutDown(\"%s\")\n", pwd);
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
		    
		    System.out.println("Server started! Waiting for incoming requests...");
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
}