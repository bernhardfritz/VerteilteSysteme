package rmicomputation;

import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Client {
	
	public static void main(String[] args) {
		try {
			 // get registry and create serverStub from registry lookup
			Registry registry = LocateRegistry.getRegistry();
		    RemoteServer serverStub = (RemoteServer) registry.lookup("Server");
			
			// create a callbackClient for deepThought execution
		    Callback callbackClient = new CallbackClient();
		    UnicastRemoteObject.exportObject(callbackClient, 0);
		    
		    String question = "What is the meaning of life?";
		    System.out.printf("Executing DeepThought(\"%s\")\n", question);
		    serverStub.deepThought(question, callbackClient);
		    
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
		    while(!callbackClient.isShutdown()) {
		    	Thread.sleep(1000);
		    }
		    
		    System.out.println(callbackClient.getAnswer());
		    
		    
		    System.out.printf("Server ShutDown(\"now\"): %s\n", serverStub.shutDown("now"));
		    
		    // unexport callbackClient to shut down gracefully
		    UnicastRemoteObject.unexportObject(callbackClient, true);
		    
		} catch (ConnectException e) {
		    System.err.println("Server has to be started first!");
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
}