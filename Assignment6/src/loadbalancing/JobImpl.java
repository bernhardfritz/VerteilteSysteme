package loadbalancing;

public class JobImpl<T> implements Job<T> {
	
	private static final long serialVersionUID = 1881720638050273071L;
	
	private T result;

	@Override
	public boolean isDone() {
		return result != null;
	}

	@Override
	public void setResult(Object result) {
		this.result = (T) result;
	}

	@Override
	public T getResult() {
		if (!isDone()) {
			throw new IllegalStateException();
		}
		return result;
	}
}