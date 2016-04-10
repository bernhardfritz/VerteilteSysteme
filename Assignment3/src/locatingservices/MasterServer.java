package locatingservices;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.gson.Gson;

public class MasterServer {
	private Protocol protocol;
	private Map<String, Set<String>> map;
	private boolean shutdown;
	
	public MasterServer() {
		protocol = new Protocol();
		map = new HashMap<String, Set<String>>();
		shutdown = false;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				shutdown();
			}
		});
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
				List<String> returnList = protocol.masterReplay(socket);
				String jsonReq = returnList.get(0);
				String address = returnList.get(1).split(":")[0];
				String port = returnList.get(1).split(":")[1];
				System.out.println("Received a packet! Checking type...");
				AnnounceServiceRequest announceServiceReq = gson.fromJson(jsonReq, AnnounceServiceRequest.class);
				if(announceServiceReq.getService() != null) {
					String service = announceServiceReq.getService();
					int serverSocketPort = announceServiceReq.getServerSocketPort();
					System.out.printf("It's a AnnounceServiceRequest packet sent by server(%s:%d). Handling request...\n", address, serverSocketPort);
					if(!map.containsKey(service)) map.put(service, new HashSet<String>());
					map.get(service).add(address+":"+serverSocketPort);
					System.out.printf("Service(%s), hosted by server(%s:%d), has been added to available services.\n", service, address, serverSocketPort);
				}
				else {
					AvailableServicesRequest availableServicesReq = gson.fromJson(jsonReq, AvailableServicesRequest.class);
					if(availableServicesReq.getMessage() != null) {
						System.out.printf("It's a AvailableServicesRequest packet sent by client(%s:%s). Handling request...\n", address, port);
						String[] services = map.keySet().toArray(new String[0]);
						AvailableServicesResponse availableServicesRes = new AvailableServicesResponse(services);
						System.out.printf("Sending AvailableServicesResponse to client(%s:%s) containing following services: %s\n", address, port, Arrays.toString(services));
						protocol.masterRespond(socket, gson.toJson(availableServicesRes), address, port);
						System.out.println("AvailableServicesResponse has been sent!");
					} else {
						LocateServiceRequest locateServiceReq = gson.fromJson(jsonReq, LocateServiceRequest.class);
						if (locateServiceReq.getService() != null) {
							System.out.printf("It's a LocateServiceRequest packet sent by client(%s:%s). Handling request...\n", address, port);
							String randomServiceProvider = getRandomElementOfSet(map.get(locateServiceReq.getService()));
							LocateServiceResponse locateServiceRes = new LocateServiceResponse(randomServiceProvider.split(":")[0], Integer.parseInt(randomServiceProvider.split(":")[1]));
							System.out.println("Sending LocateServiceResponse containing following service provider: " + randomServiceProvider);
							protocol.masterRespond(socket, gson.toJson(locateServiceRes), address, port);
							System.out.println("LocateServiceResponse has been sent!");
						}
						else {
							System.out.println("It's not a package for the masterserver.");
						}
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
		System.out.println("Starting masterserver...");
		MasterServer server = new MasterServer();
		server.listen();
	}
}