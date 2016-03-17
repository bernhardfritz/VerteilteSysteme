package singlethreaded;

public class OperationRequest {
	private Operation operation;
	private int[] operators;
	
	public OperationRequest(Operation operation, int[] operators) {
		this.operation = operation;
		this.operators = operators;
	}
}
