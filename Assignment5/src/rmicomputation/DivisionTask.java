package rmicomputation;

public class DivisionTask implements Task<Integer> {
	private static final long serialVersionUID = 8057120085546117823L;
	private int a, b;
	public DivisionTask(int a, int b) {
		this.a = a;
		this.b = b;
	}
	
	@Override
	public Integer execute() {
		if(b == 0) return null;
		return a/b;
	}
	
}
