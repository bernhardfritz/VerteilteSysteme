package locatingservices;

public class SubtractionService implements Service<Integer> {
	@Override
	public String getName() {
		return "SUBTRACTION";
	}

	@Override
	public Integer execute(Integer... args) {
		return args[0] - args[1];
	}
	
	// Example
	public static void main(String[] args) {
		Service subtractionService = new SubtractionService();
		System.out.println(subtractionService.getName());
		System.out.println(((Service<Integer>) subtractionService).execute(2, 1));
	}
}
