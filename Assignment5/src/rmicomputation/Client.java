package rmicomputation;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
	
	public static void main(String[] args) {
		try {
		    Registry registry = LocateRegistry.getRegistry();
		    RemoteServer stub = (RemoteServer) registry.lookup("Server");
		    
		    int a = 6;
		    int b = 3;
		    System.out.printf("%d + %d = %d\n", a, b, stub.addition(a, b));
		    System.out.printf("%d - %d = %d\n", a, b, stub.subtraction(a, b));
		    System.out.printf("%d * %d = %d\n", a, b, stub.multiplication(a, b));
		    System.out.printf("lucas(%d) = %d\n", b, stub.lucas(b));
		    System.out.printf("%d / %d = %d\n", a, b, stub.executeTask(new DivisionTask(a, b)));
		    System.out.printf("Executing HelloWorldTask: %s\n", stub.executeTask(new HelloWorldTask()));
		    System.out.printf("Fibonacci(%d) = %d\n", a, stub.executeTask(new FibonacciTask(a)));
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
}