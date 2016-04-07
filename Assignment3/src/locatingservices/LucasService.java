package locatingservices;

public class LucasService implements Service<Integer> {
	@Override
	public String getName() {
		return "LUCAS";
	}

	@Override
	public Integer execute(Integer... args) {
		if(args[0] == 0) return 2;
		if(args[0] == 1) return 1;
		else return execute(args[0] - 1) + execute(args[0] - 2);
	}
	
	// Example
	public static void main(String[] args) {
		Service<Integer> lucasService = new LucasService();
		System.out.println(lucasService.getName());
		System.out.println(lucasService.execute(3));
	}
}
