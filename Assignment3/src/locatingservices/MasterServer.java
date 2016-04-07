package locatingservices;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;

public class MasterServer {
	private Protocol protocol;
	private Map<String, Set<InetAddress>> map;
	private boolean shutdown;
	
	public MasterServer() {
		protocol = new Protocol();
		map = new HashMap<String, Set<InetAddress>>();
		shutdown = false;
	}
	
	private <T> T getRandomElementOfSet(Set<T> set) {
		List<T> list = new ArrayList<T>();
		list.addAll(set);
		return list.get(new Random().nextInt(set.size()));
	}
	
	public void listen() {
		Gson gson = new Gson();
		DatagramSocket socket = null;
 		try {
 			socket = new DatagramSocket(protocol.PORT);
			System.out.println("Server started! Listening to port " + protocol.PORT);
			while(!shutdown) {
				System.out.println("Waiting for incoming packets...");
				String jsonReq = protocol.masterReplay(socket);
				System.out.println("Received a packet! Checking type...");
				AnnounceServiceRequest announceServiceReq = gson.fromJson(jsonReq, AnnounceServiceRequest.class);
				if(!announceServiceReq.getService().isEmpty()) {
					InetAddress serverAddress = socket.getInetAddress();
					System.out.printf("It's a AnnounceServiceRequest packet sent by server(%s). Handling request...\n", serverAddress.getHostAddress());
					String service = announceServiceReq.getService();
					if(!map.containsKey(service)) map.put(service, new HashSet<InetAddress>());
					map.get(service).add(serverAddress);
					System.out.printf("Service(%s), hosted by server(%s), has been added to available services.\n", service, serverAddress.getHostAddress());
				}
				else {
					InetAddress clientAddress = socket.getInetAddress();
					AvailableServicesRequest availableServicesReq = gson.fromJson(jsonReq, AvailableServicesRequest.class);
					if(!availableServicesReq.getMessage().isEmpty()) {
						System.out.printf("It's a AvailableServicesRequest packet sent by client(%s). Handling request...\n", clientAddress.getHostAddress());
						String[] services = map.keySet().toArray(new String[0]);
						AvailableServicesResponse availableServicesRes = new AvailableServicesResponse(services);
						System.out.printf("Sending AvailableServicesResponse to client(%s) containing following services: %s\n", clientAddress.getHostAddress(), Arrays.toString(services));
						protocol.masterRespond(socket, gson.toJson(availableServicesRes), clientAddress);
						System.out.println("AvailableServicesResponse has been sent!");
					} else {
						System.out.printf("It's a LocateServiceRequest packet sent by client(%s). Handling request...\n", clientAddress.getHostAddress());
						LocateServiceRequest locateServiceReq = gson.fromJson(jsonReq, LocateServiceRequest.class);
						String randomServiceProvider = getRandomElementOfSet(map.get(locateServiceReq.getService())).getHostAddress();
						LocateServiceResponse locateServiceRes = new LocateServiceResponse(randomServiceProvider);
						System.out.println("Sending LocateServiceResponse containing following service provider: " + randomServiceProvider);
						protocol.masterRespond(socket, gson.toJson(locateServiceRes), clientAddress);
						System.out.println("LocateServiceResponse has been sent!");
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}
	}
	
	// shutdown
	public void shutdown() {
		shutdown = true;
		System.out.println("Shutting down...");
	}
	
	public static void main(String[] args) {
		System.out.println("Starting server...");
		final MasterServer server = new MasterServer();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				server.shutdown();
			}
		});
		server.listen();
	}
}
