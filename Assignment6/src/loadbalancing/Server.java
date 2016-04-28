package loadbalancing;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server implements RemoteServer {
	
	private String name;
	
	private static final int CAPACITY = 3;
	private static final int N_THREADS = 5;
	
	private LinkedBlockingQueue<Runnable> queue;
	private ThreadPoolExecutor executor;
	
	public Server(String name) {
		queue = new LinkedBlockingQueue<Runnable>(CAPACITY);
		executor = new ThreadPoolExecutor(N_THREADS, N_THREADS, 0L, TimeUnit.MILLISECONDS, queue);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		this.name = name;
	}

	@Override
	public <T> Job<T> submit(final UUID idParam, Callable<T> callable, final RemoteClient callbackParam) throws RemoteException {
		System.out.println(callbackParam.getName() + " trying to submit task(" + idParam + ").");
		
		final Future<T> resultParam;
		
		try {
			resultParam = executor.submit(callable);
			System.out.println("Job(" + idParam + ") from " + callbackParam.getName() + " successfully submitted.");
		} catch (RejectedExecutionException e) {
			System.out.println("Job(" + idParam + ") from " + callbackParam.getName() + " rejected.");
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
	
	@Override
	public String getName() {
		return name;
	}
	
	public static void main(String args[]) {
		try {
		    Server server = new Server((args.length > 0) ? args[0] : "Server"+new Random().nextInt(1000));
		    RemoteServer serverStub = (RemoteServer) UnicastRemoteObject.exportObject(server, 0);

			Registry registry = LocateRegistry.getRegistry();
		    RemoteDispatcher dispatcherStub = (RemoteDispatcher) registry.lookup("Dispatcher");

		    System.out.println("Registering at " + dispatcherStub.getName() + "...");
		    dispatcherStub.register(serverStub);
		    System.out.println(server.getName() + " started! Waiting for incoming requests...\n");
		} catch (ConnectException e) {
		    System.err.println("Dispatcher has to be started first!");
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
}