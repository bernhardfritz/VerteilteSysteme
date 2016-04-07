package locatingservices;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.gson.Gson;

// JSON request
// {
//   "service": "ADDITION"
//   "address": "127.0.0.1"
// }

public class AnnounceServiceRequest {
	private String service;
	private String address;
	
	public AnnounceServiceRequest(String service, String address) {
		this.service = service;
		this.address = address;
	}
	
	public String getService() {
		return service;
	}
	
	public InetAddress getInetAddress() {
		try {
			return InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// Example
	public static void main(String[] args) {
		AnnounceServiceRequest req = new AnnounceServiceRequest("ADDITION", "127.0.0.1");
		Gson gson = new Gson();
		String json = gson.toJson(req);
		System.out.println(json);
	}
}
