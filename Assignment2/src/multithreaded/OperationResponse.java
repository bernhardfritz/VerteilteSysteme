package multithreaded;

import com.google.gson.Gson;

public class OperationResponse {
	private int result;
	
	public OperationResponse(int result) {
		this.result = result;
	}
	
	public int getResult() {
		return result;
	}
	
	// Example
	public static void main(String[] args) {
		String json = "{\"result\":3}";
		Gson gson = new Gson();
		OperationResponse res = gson.fromJson(json, OperationResponse.class);
		System.out.println(res.getResult());
	}
}
