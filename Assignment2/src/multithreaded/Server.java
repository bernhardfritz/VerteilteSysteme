package multithreaded;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
	private Protocol protocol;
	List<String> database;
	private static volatile boolean shutdown;

	private static final int CAPACITY = 500;
	private static final int N_THREADS = 500;

	public Server(Protocol protocol) {
		this.protocol = protocol;
		database = new ArrayList<String>();

		// fill user database
		database.add("admin");
		for (int i = 0; i < 10000; i++) {
			if ((i % 3) == 0) {
				database.add("Client" + i);
			}
		}

		shutdown = false;
	}

	public void listen() {
		// Open socket
		ServerSocket listener = null;

		try {
			listener = new ServerSocket(protocol.port);
			System.out.println("Server started! Listening on 127.0.0.1:9090");

			// set socket timeout
			listener.setSoTimeout(5000);

			// setup executor service

			// create queue
			LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(CAPACITY);
			// create executor
			final ThreadPoolExecutor executor = new ThreadPoolExecutor(N_THREADS, N_THREADS, 0L, TimeUnit.MILLISECONDS,
					queue);
			executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
			// create unconfigurable executor service
			ExecutorService es = Executors.unconfigurableExecutorService(executor);

			while (!shutdown) {
				System.out.println("Waiting for incoming connections...");
				// wait for client connections

				Socket socket = null;
				try {
					socket = listener.accept();
					// socket timeout
				} catch (SocketTimeoutException e) {
					System.out.println("Socket timeout!");
					continue;
				}

				// create new request handler
				RequestHandler rh = new RequestHandler(this.protocol, this.database, socket);

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
		System.out.println("Shutting down...");
	}

	public static void main(String[] args) {
		System.out.println("Starting server...");
		Server server = new Server(new Protocol(9090));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				Server.shutdown();
			}
		});
		server.listen();
	}

}
