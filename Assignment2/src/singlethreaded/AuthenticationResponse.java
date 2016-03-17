package singlethreaded;

public class AuthenticationResponse {
	private boolean success;
	
	public AuthenticationResponse(boolean success) {
		this.success = success;
	}
	
	public boolean isSuccess() {
		return success;
	}
}
