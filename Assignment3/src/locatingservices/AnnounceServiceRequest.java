package locatingservices;

import com.google.gson.Gson;

// JSON request
// {
//   "announce": "ADDITION"
// }

public class AnnounceServiceRequest {
	private String announce;
	
	public AnnounceServiceRequest(String service) {
		announce = service;
	}
	
	public String getService() {
		return announce;
	}
	
	// Example
	public static void main(String[] args) {
		AnnounceServiceRequest req = new AnnounceServiceRequest("ADDITION");
		Gson gson = new Gson();
		String json = gson.toJson(req);
		System.out.println(json);
	}
}
