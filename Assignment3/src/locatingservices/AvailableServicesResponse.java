package locatingservices;

import java.util.Arrays;

import com.google.gson.Gson;

// JSON response
// {
//   "services": ["ADDITION", "SUBTRACTION", "MULTIPLICATION", "LUCAS"]
// }

public class AvailableServicesResponse {
	private String[] services;
	
	public AvailableServicesResponse(String[] services) {
		this.services = services;
	}
	
	public String[] getServices() {
		return services;
	}
	
	// Example
	public static void main(String[] args) {
		String json = "{\"services\": [\"ADDITION\", \"SUBTRACTION\", \"MULTIPLICATION\", \"LUCAS\"]}";
		Gson gson = new Gson();
		AvailableServicesResponse res = gson.fromJson(json, AvailableServicesResponse.class);
		System.out.println(Arrays.toString(res.getServices()));
	}
}
