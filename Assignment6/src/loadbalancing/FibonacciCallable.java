package loadbalancing;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class FibonacciCallable implements Callable<Integer>, Serializable {
	
	private static final long serialVersionUID = 6480181892260629531L;
	
	private int n;
	
	public FibonacciCallable(int n) {
		this.n = n;
	}
	
	private Integer fibonacci(int n) {
		if(n <= 0) return null;
		if(n <= 2) return 1;
		return fibonacci(n-1) + fibonacci(n-2);
	}

	@Override
	public Integer call() throws Exception {
		return fibonacci(n);
	}
}