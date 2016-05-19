package socket;

import java.math.BigInteger;

public class OperationResponse {
	private BigInteger result;
	
	public OperationResponse(BigInteger result) {
		this.result = result;
	}
	
	public BigInteger getResult() {
		return result;
	}
	
}
