package multithreaded;

import com.google.gson.Gson;

public class OperationRequest {
	private Operation operation;
	private int[] operators;
	
	public OperationRequest(Operation operation, int[] operators) {
		this.operation = operation;
		this.operators = operators;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public int[] getOperators() {
		return operators;
	}
	
	// Example
	public static void main(String[] args) {
		int[] operators = {1,2};
		OperationRequest req = new OperationRequest(Operation.ADDITION, operators);
		Gson gson = new Gson();
		String json = gson.toJson(req);
		System.out.println(json);
	}
}
