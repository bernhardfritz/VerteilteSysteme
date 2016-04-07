package locatingservices;

import com.google.gson.Gson;

public class ServiceResponse<T> {
	private T result;
	
	public ServiceResponse(T result) {
		this.result = result;
	}
	
	public T getResult() {
		return result;
	}
	
	// Example
	public static void main(String[] args) {
		String json = "{\"result\":3}";
		Gson gson = new Gson();
		ServiceResponse<?> res = gson.fromJson(json, ServiceResponse.class);
		System.out.println(res.getResult());
	}
}
