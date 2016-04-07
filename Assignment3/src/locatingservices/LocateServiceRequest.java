package locatingservices;

import com.google.gson.Gson;

// JSON request
// {
//   "service": "ADDITION"
// }

public class LocateServiceRequest {
	private String service;
	
	public LocateServiceRequest(String service) {
		this.service = service;
	}
	
	public String getService() {
		return service;
	}

	// Example
	public static void main(String[] args) {
		LocateServiceRequest req = new LocateServiceRequest("ADDITION");
		Gson gson = new Gson();
		String json = gson.toJson(req);
		System.out.println(json);
	}
}
