package computationservice;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server implements RemoteServer {
	
	private static final int CAPACITY = 3;
	private static final int N_THREADS = 5;
	
	private LinkedBlockingQueue<Runnable> queue;
	private ThreadPoolExecutor executor;
	
	public Server() {
		queue = new LinkedBlockingQueue<Runnable>(CAPACITY);
		executor = new ThreadPoolExecutor(N_THREADS, N_THREADS, 0L, TimeUnit.MILLISECONDS, queue);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
	}

	@Override
	public <T> Job<T> submit(UUID idParam, Callable<T> callable, RemoteClient callbackParam) throws RemoteException {
		Future<T> resultParam;
		
		try {
			resultParam = executor.submit(callable);
		} catch (RejectedExecutionException e) {
			return null;
		}
		
		System.out.println("Active jobs: " + executor.getActiveCount());
		System.out.println("Waiting jobs: " + queue.size() + "\n");
		
		new Thread() {
			private UUID id;
			private RemoteClient callback;
			private Future<T> result;
			
			public void run() {
				id = idParam;
				callback = callbackParam;
				result = resultParam;
				
				try {
					T r = result.get();
					Thread.sleep(500);
					callback.callback(r, id);
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
		    Server server = new Server();
		    RemoteServer serverStub = (RemoteServer) UnicastRemoteObject.exportObject(server, 0);

		    Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
		    registry.rebind("Server", serverStub);
		    
		    System.out.println("Server started! Waiting for incoming requests...");
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
}