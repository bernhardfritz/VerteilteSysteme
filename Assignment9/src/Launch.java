import java.net.MalformedURLException;

public class Launch {
	public static void main(String[] args) throws InterruptedException {
		
		// Socket
		Thread server = new Thread() {
			public void run() {
				socket.Server.main(null);
			}
		};
		Thread client = new Thread() {
			public void run() {
				socket.Client.main(null);
			}
		};
		server.start();
		Thread.sleep(100);
		client.start();
		client.join();
		server.join();
		
		// RMI
		server = new Thread() {
			public void run() {
				rmi.Server.main(null);
			}
		};
		client = new Thread() {
			public void run() {
				rmi.Client.main(null);
			}
		};
		server.start();
		Thread.sleep(100);
		client.start();
		client.join();
		server.join();
		
		// Webservice
		server = new Thread() {
			public void run() {
				webservice.Server.main(null);
			}
		};
		client = new Thread() {
			public void run() {
				try {
					webservice.Client.main(null);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		};
		server.start();
		Thread.sleep(500); // WebServer braucht lange zum Starten
		client.start();
		client.join();
		server.join();
		
		// Direkt
		direct.Main.main(null);
	}
}
