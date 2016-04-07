package locatingservices;

import com.google.gson.Gson;

// JSON request
// {
//   "locate": "ADDITION"
// }

public class LocateServiceRequest {
	private String locate;
	
	public LocateServiceRequest(String service) {
		locate = service;
	}
	
	public String getService() {
		return locate;
	}

	// Example
	public static void main(String[] args) {
		LocateServiceRequest req = new LocateServiceRequest("ADDITION");
		Gson gson = new Gson();
		String json = gson.toJson(req);
		System.out.println(json);
	}
}
