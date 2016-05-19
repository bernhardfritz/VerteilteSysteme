import java.net.MalformedURLException;

public class Launch {
	public static void main(String[] args) throws InterruptedException {
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		server.start();
		Thread.sleep(500); // WebServer braucht lange zum starten.
		client.start();
		client.join();
		server.join();
		direct.Main.main(null);
	}
}
