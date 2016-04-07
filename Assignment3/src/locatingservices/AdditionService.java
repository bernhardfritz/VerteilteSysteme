package locatingservices;

public class AdditionService implements Service<Integer>{
	@Override
	public String getName() {
		return "ADDITION";
	}

	@Override
	public Integer execute(Integer... args) {
		return args[0] + args[1];
	}
	
	// Example
	public static void main(String[] args) {
		Service<Integer> additionService = new AdditionService();
		System.out.println(additionService.getName());
		System.out.println(additionService.execute(1, 2));
	}
}
