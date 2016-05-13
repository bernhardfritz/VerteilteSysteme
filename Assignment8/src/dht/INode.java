package dht;

public interface INode {

	public void join(Node node);
	
	public void sendMSG(String msg, Node target);
	
}