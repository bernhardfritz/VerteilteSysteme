package chord;

public interface INode {

	public void join(Node node);
	
	public void sendMSG(String msg, int target);
	
}
