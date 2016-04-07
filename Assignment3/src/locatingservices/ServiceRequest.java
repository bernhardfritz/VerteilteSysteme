package locatingservices;

import com.google.gson.Gson;

// JSON request
// {
//   "service": "ADDITION",
//   "args": [1,2]
// }

public class ServiceRequest<T> {
	private String service;
	private T[] args;
	
	public ServiceRequest(String service, T[] args) {
		this.service = service;
		this.args = args;
	}
	
	public String getService() {
		return service;
	}
	
	public T[] getArgs() {
		return args;
	}
	
	// Example
	public static void main(String[] args) {
		Integer[] _args = {1,2};
		ServiceRequest<Integer> req = new ServiceRequest<Integer>("ADDITION", _args);
		Gson gson = new Gson();
		String json = gson.toJson(req);
		System.out.println(json);
	}
}
