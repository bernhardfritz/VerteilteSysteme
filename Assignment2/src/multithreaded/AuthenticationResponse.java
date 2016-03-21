package multithreaded;

import com.google.gson.Gson;

public class AuthenticationResponse {
	private boolean success;
	
	public AuthenticationResponse(boolean success) {
		this.success = success;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	// Example
	public static void main(String[] args) {
		String json = "{\"success\":true}";
		Gson gson = new Gson();
		AuthenticationResponse res = gson.fromJson(json, AuthenticationResponse.class);
		System.out.println(res.isSuccess());
	}
}
