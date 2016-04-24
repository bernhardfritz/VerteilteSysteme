package rmicomputation;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Client implements RemoteClient {
	private String asyncResult = "";
	
	@Override
	public void callback(String s) throws RemoteException {
		asyncResult = s;
	}
	
	public boolean asyncResultReceived() {
		return !asyncResult.isEmpty();
	}
	
	public String getAsyncResult() {
		String tmp = asyncResult;
		asyncResult = "";
		return tmp;
	}
	
	public static void main(String[] args) {
		Client client = new Client();
		try {
			 // get registry and create serverStub from registry lookup
			Registry registry = LocateRegistry.getRegistry();
		    RemoteServer serverStub = (RemoteServer) registry.lookup("Server");
			
			// create a callbackClient for deepThought execution
		    RemoteClient clientStub = (RemoteClient) UnicastRemoteObject.exportObject(client, 0);
		    
		    String question = "What is the meaning of life?";
		    System.out.printf("Executing DeepThought(\"%s\")\n", question);
		    serverStub.deepThought(question, clientStub);
		    
		    int a = 6;
		    int b = 3;
		    System.out.printf("%d + %d = %d\n", a, b, serverStub.addition(a, b));
		    System.out.printf("%d - %d = %d\n", a, b, serverStub.subtraction(a, b));
		    System.out.printf("%d * %d = %d\n", a, b, serverStub.multiplication(a, b));
		    System.out.printf("Lucas(%d) = %d\n", b, serverStub.lucas(b));
		    System.out.printf("%d / %d = %d\n", a, b, serverStub.executeTask(new DivisionTask(a, b)));
		    System.out.printf("Executing HelloWorldTask: %s\n", serverStub.executeTask(new HelloWorldTask()));
		    System.out.printf("Fibonacci(%d) = %d\n", a, serverStub.executeTask(new FibonacciTask(a)));
		    
		    System.out.printf("Server ShutDown(\"test\"): %s\n", serverStub.shutDown("test"));
		    
		    // wait for callback
		    while(!client.asyncResultReceived()) {
		    	Thread.sleep(1000);
		    }
		    
		    System.out.println(client.getAsyncResult());
		    
		    
		    System.out.printf("Server ShutDown(\"now\"): %s\n", serverStub.shutDown("now"));
		    
		    // unexport callbackClient to shut down gracefully
		    UnicastRemoteObject.unexportObject(client, true);
		    
		} catch (ConnectException e) {
		    System.err.println("Server has to be started first!");
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
}