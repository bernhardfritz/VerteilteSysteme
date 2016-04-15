package p2pnetwork;

import com.google.gson.Gson;

public class ShallowNode {
	private String ip;
	private int port;
	private String name;
	
	public ShallowNode(String ip, int port, String name) {
		this.ip = ip;
		this.port = port;
		this.name = name;
	}
	
	public String getIp() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ShallowNode)) {
			return false;
		}
		ShallowNode other = (ShallowNode) o;
		boolean sameIp = this.ip.equals(other.ip);
		boolean samePort = this.port == other.port;
		boolean sameName = this.name.equals(other.name);
		return sameIp && samePort && sameName;
	}
	
	@Override
	public int hashCode() {
		int hash = 17;
		hash = 31 * hash + ip.hashCode();
		hash = 31 * hash + port;
		hash = 31 * hash + name.hashCode();
		return hash;
	}
	
	@Override
	public String toString() {
		return String.format("%s(%s:%d)", name, ip, port);
	}
	
	public static void main(String[] args) {
		System.out.println(new Gson().toJson(new ShallowNode("127.0.0.1", 9090, "foo")));
		System.out.println(new ShallowNode("127.0.0.1", 9090, "foo").equals(new ShallowNode("127.0.0.1", 9090, "foo")));
	}
}
