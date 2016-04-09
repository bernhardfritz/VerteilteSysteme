package locatingservices;

import com.google.gson.Gson;

public class OperationRequest {
	private String operation;
	private int[] operators;
	
	public OperationRequest(String operation, int[] operators) {
		this.operation = operation;
		this.operators = operators;
	}
	
	public String getOperation() {
		return operation;
	}
	
	public int[] getOperators() {
		return operators;
	}
	
	// Example
	public static void main(String[] args) {
		int[] operators = {1,2};
		OperationRequest req = new OperationRequest("ADDITION", operators);
		Gson gson = new Gson();
		String json = gson.toJson(req);
		System.out.println(json);
	}
}
