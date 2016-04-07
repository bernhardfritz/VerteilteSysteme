package locatingservices;

import com.google.gson.Gson;

// JSON request
// {
//   "message": "What services are available?"
// }

public class AvailableServicesRequest {
	private String message;
	
	public AvailableServicesRequest(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	// Example
	public static void main(String[] args) {
		AvailableServicesRequest req = new AvailableServicesRequest("What services are available?");
		Gson gson = new Gson();
		String json = gson.toJson(req);
		System.out.println(json);
	}
}
