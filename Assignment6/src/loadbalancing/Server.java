package loadbalancing;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server implements RemoteServer {
	
	private static final int CAPACITY = 5;
	private static final int N_THREADS = 5;
	
	private LinkedBlockingQueue<Runnable> queue;
	private ThreadPoolExecutor executor;
	
	private ExecutorService es;
	
	public Server() {
		queue = new LinkedBlockingQueue<Runnable>(CAPACITY);
		executor = new ThreadPoolExecutor(N_THREADS, N_THREADS, 0L, TimeUnit.MILLISECONDS, queue);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		
		es = Executors.unconfigurableExecutorService(executor);
	}

	@Override
	public <T> Job<T> submit(UUID idParam, Callable<T> callable, RemoteClient callbackParam) throws RemoteException {
		Future<T> resultParam;
		
		try {
			resultParam = es.submit(callable);
		} catch (RejectedExecutionException e) {
			return null;
		}
		
		new Thread() {
			private UUID id;
			private RemoteClient callback;
			private Future<T> result;
			
			public void run() {
				id = idParam;
				callback = callbackParam;
				result = resultParam;
				
				try {
					Thread.sleep(500);
					callback.callback(result.get(), id);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}.start();
			
		return new JobImpl<T>();
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