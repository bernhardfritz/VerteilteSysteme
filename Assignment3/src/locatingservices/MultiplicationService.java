package locatingservices;

public class MultiplicationService implements Service<Integer> {
	@Override
	public String getName() {
		return "MULTIPLICATION";
	}

	@Override
	public Integer execute(Integer... args) {
		return args[0] * args[1];
	}
	
	// Example
	public static void main(String[] args) {
		Service multiplicationService = new MultiplicationService();
		System.out.println(multiplicationService.getName());
		System.out.println(((Service<Integer>) multiplicationService).execute(2, 3));
	}
}
