package socket;

import java.math.BigInteger;

public class OperationRequest {
	private Operation operation;
	private BigInteger[] operators;
	
	public OperationRequest(Operation operation, BigInteger[] operators) {
		this.operation = operation;
		this.operators = operators;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public BigInteger[] getOperators() {
		return operators;
	}
}
