package rmi;

public class HelloWorldTask implements Task<String>{
	
	private static final long serialVersionUID = -5812013226547465671L;

	@Override
	public String execute() {
		return "Hello World!";
	}
}