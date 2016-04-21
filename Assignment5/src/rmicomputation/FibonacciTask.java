package rmicomputation;

public class FibonacciTask implements Task<Integer> {
	private static final long serialVersionUID = 1849917765012457694L;
	private int n;
	
	public FibonacciTask(int n) {
		this.n = n;
	}
	
	private Integer fibonacci(int n) {
		if(n <= 0) return null;
		if(n <= 2) return 1;
		return fibonacci(n-1) + fibonacci(n-2);
	}
	
	@Override
	public Integer execute() {
		return fibonacci(n);
	}
}
