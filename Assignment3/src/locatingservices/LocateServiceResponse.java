package locatingservices;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.gson.Gson;

// JSON response
// {
//   "address": "127.0.0.1",
//	 "port": 9090
// }

public class LocateServiceResponse {
	private String address;
	private int port;
	
	public LocateServiceResponse(String address, int port) {
		this.address = address;
		this.port = port;
	}
	
	public InetAddress getInetAddress() {
		try {
			return InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public int getPort() {
		return port;
	}
	
	// Example
	public static void main(String[] args) {
		String json = "{\"address\": \"127.0.0.1\", \"port\": 9090}";
		System.out.println(json);
		Gson gson = new Gson();
		LocateServiceResponse res = gson.fromJson(json, LocateServiceResponse.class);
		System.out.println(res.getInetAddress().getHostAddress() + ":" + res.getPort());
	}
}
