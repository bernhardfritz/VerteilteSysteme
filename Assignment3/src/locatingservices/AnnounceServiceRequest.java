package locatingservices;

import com.google.gson.Gson;

// JSON request
// {
//   "announce": "ADDITION",
//	 "port": 9090
// }

public class AnnounceServiceRequest {
	private String announce;
	private int port;
	
	public AnnounceServiceRequest(String service, int serverSocketPort) {
		announce = service;
		this.port = serverSocketPort;
	}
	
	public String getService() {
		return announce;
	}
	
	public int getServerSocketPort() {
		return port;
	}
	
	// Example
	public static void main(String[] args) {
		AnnounceServiceRequest req = new AnnounceServiceRequest("ADDITION", 9090);
		Gson gson = new Gson();
		String json = gson.toJson(req);
		System.out.println(json);
	}
}
