package locatingservices;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

import com.google.gson.Gson;

public class ClientImpl implements Client, Runnable {
	private Protocol protocol;

	public ClientImpl() {
		this.protocol = new Protocol();

	}

	public void handleRequest() {
		int result = 0;
		Gson gson = new Gson();
		try {
			DatagramSocket datagramSocket = new DatagramSocket();
			
			// search for available services
			AvailableServicesRequest avServReq = new AvailableServicesRequest("What services are available?");
			String jsonAvServReq = gson.toJson(avServReq);
			System.out.println(Thread.currentThread().getId() + ": Ask for available services...");
			String jsonAvServRes = protocol.requestServiceList(datagramSocket, jsonAvServReq);
			if (jsonAvServRes == null || jsonAvServRes.isEmpty()) {
				System.out.println(Thread.currentThread().getId() + ": No services available.");
				return;
			}
			AvailableServicesResponse avServRes = gson.fromJson(jsonAvServRes, AvailableServicesResponse.class);
			String[] avaliableServices = avServRes.getServices();
			if (avaliableServices.length == 0) {
				System.out.println(Thread.currentThread().getId() + ": No services available.");
				return;
			}
			System.out.println(Thread.currentThread().getId() + ": Available services: " + Arrays.toString(avaliableServices));
			
			// choose random service
			String randomService = avaliableServices[new Random().nextInt(avaliableServices.length)];
			
			// find service provider for chosen service
			LocateServiceRequest locServReq = new LocateServiceRequest(randomService);
			String jsonLocServReq = gson.toJson(locServReq);
			String jsonLocServRes = protocol.locateService(datagramSocket, jsonLocServReq);
			System.out.printf("%d: Find server for randomly chosen service(%s)...\n", Thread.currentThread().getId(), randomService);
			LocateServiceResponse locateServiceRes = gson.fromJson(jsonLocServRes, LocateServiceResponse.class);
			String serverAddress = locateServiceRes.getInetAddress().getHostAddress();
			int serverPort = locateServiceRes.getPort();
			System.out.println(Thread.currentThread().getId() + ": Received service provider: " + serverAddress + ":" + serverPort);
			
			System.out.printf("%d: Connecting to server %s:%d...\n", Thread.currentThread().getId(), serverAddress, serverPort);
			try {
				// Open connection to server
				Socket socket = new Socket(serverAddress, serverPort);
				try {
					System.out.println(Thread.currentThread().getId() + ": Connected!");
					// Create JSON operation request using parameters operation and operators
					int[] operators = {new Random().nextInt(100), new Random().nextInt(100)};
					
					if (randomService.equals("LUCAS")) {
						operators = new int[]{new Random().nextInt(20)};
					}
					OperationRequest opReq = new OperationRequest(randomService, operators);
					String jsonOpReq = gson.toJson(opReq);
					System.out.printf("%d: Sending operation request %s %s...\n", Thread.currentThread().getId(), randomService, Arrays.toString(operators));
					// Use protocol's request method to send request
					String jsonOpRes = protocol.request(socket, jsonOpReq);
					// Unwrap JSON response
					OperationResponse opRes = gson.fromJson(jsonOpRes, OperationResponse.class);
					result = opRes.getResult();
					System.out.println(Thread.currentThread().getId() + ": Received result: " + result);
				} finally {
					System.out.println(Thread.currentThread().getId() + ": Closing connection...");
					// finally close connection
					socket.close();	
				}
			} catch (SocketException e) {
				System.out.println(Thread.currentThread().getId() + ": Server is not available.");
			}
		} catch (SocketException e) {
			System.out.println(Thread.currentThread().getId() + ": Socket has been shut down!");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		handleRequest();
	}

	public static void main(String[] args) {
		//create and start 10 clients
		for (int i = 0; i < 10; i++) {
			//create client
			ClientImpl client = new ClientImpl();
			
			//create and start thread
			new Thread(client).start();
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}