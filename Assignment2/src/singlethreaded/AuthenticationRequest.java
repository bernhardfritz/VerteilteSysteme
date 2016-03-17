package singlethreaded;

import com.google.gson.Gson;

public class AuthenticationRequest {
	private String username;
	
	public AuthenticationRequest(String username) {
		this.username = username;
	}
	
	// Example
	public static void main(String[] args) {
		AuthenticationRequest req = new AuthenticationRequest("foo");
		Gson gson = new Gson();
		String json = gson.toJson(req);
		System.out.println(json);
	}
}
