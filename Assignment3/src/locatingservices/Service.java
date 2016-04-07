package locatingservices;

public interface Service<T> {
	public String getName();
	public T execute(T... args);
}
