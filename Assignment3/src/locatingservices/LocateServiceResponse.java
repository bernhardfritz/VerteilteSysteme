package locatingservices;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.gson.Gson;

// JSON response
// {
//   "address": "127.0.0.1"
// }

public class LocateServiceResponse {
	private String address;
	
	public LocateServiceResponse(String address) {
		this.address = address;
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
		String json = "{\"address\": \"127.0.0.1\"}";
		System.out.println(json);
		Gson gson = new Gson();
		LocateServiceResponse res = gson.fromJson(json, LocateServiceResponse.class);
		System.out.println(res.getInetAddress().getHostAddress());
	}
}
