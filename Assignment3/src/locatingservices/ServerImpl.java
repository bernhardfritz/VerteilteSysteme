package locatingservices;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

public class ServerImpl implements Server, Runnable {
	private Protocol protocol;
	private static volatile boolean shutdown;
	private List<String> serviceList;

	private static final int CAPACITY = 500;
	private static final int N_THREADS = 500;

	public ServerImpl(List<String> serviceList) {
		this.protocol = new Protocol();
		this.serviceList = serviceList;

		shutdown = false;
	}

	public void listen() {
		Gson gson = new Gson();
		DatagramSocket datagramSocket = null;
		ServerSocket listener = null;

		try {
			datagramSocket = new DatagramSocket();
			
			listener = new ServerSocket(0);
			System.out.println(Thread.currentThread().getId() + ": Server started!");

			// setup executor service
			// create queue
			LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(CAPACITY);
			// create executor
			final ThreadPoolExecutor executor = new ThreadPoolExecutor(N_THREADS, N_THREADS, 0L, TimeUnit.MILLISECONDS, queue);
			executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
			// create unconfigurable executor service
			ExecutorService es = Executors.unconfigurableExecutorService(executor);
			
			// accounce available services
			for (String service : serviceList) {
				AnnounceServiceRequest annServReq = new AnnounceServiceRequest(service, listener.getLocalPort());
				String jsonAnnServReq = gson.toJson(annServReq);
				protocol.announceService(datagramSocket, jsonAnnServReq);
				System.out.printf("%d: Service(%s) has been announced.\n", Thread.currentThread().getId(), service);
			}

			System.out.println(Thread.currentThread().getId() + ": Listening to port " + listener.getLocalPort());
			while (!shutdown) {
				System.out.println(Thread.currentThread().getId() + ": Waiting for incoming connections...");
				// wait for client connections
				Socket socket = listener.accept();

				// create new request handler
				RequestHandler rh = new RequestHandler(Thread.currentThread().getId(), this.protocol, socket);

				// submit request handler to executor service
				es.submit(rh);
			}

			// shutdown executor service
			es.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				listener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// shutdown
	public static void shutdown() {
		shutdown = true;
		System.out.println(Thread.currentThread().getId() + ": Shutting down...");
	}
	
	@Override
	public void run() {
		System.out.println(Thread.currentThread().getId() + ": Starting server...");
		listen();
	}

	public static void main(String[] args) {
		ServerImpl s1 = new ServerImpl(Arrays.asList("ADDITION", "MULTIPLICATION"));
		ServerImpl s2 = new ServerImpl(Arrays.asList("ADDITION", "SUBTRACTION"));
		ServerImpl s3 = new ServerImpl(Arrays.asList("MULTIPLICATION", "SUBTRACTION", "LUCAS"));
		ServerImpl s4 = new ServerImpl(Arrays.asList("MULTIPLICATION", "LUCAS"));
		
		new Thread(s1).start();
		new Thread(s2).start();
		new Thread(s3).start();
		new Thread(s4).start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				ServerImpl.shutdown();
			}
		});
	}
}