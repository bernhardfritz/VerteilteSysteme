package dht;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class DHTNode implements Map<String, Object> {
	
	private Node node;
	
	private int m;
	
	public DHTNode(int id, int m) {
		this.m = m;
		this.node = new Node(id, m);
	}
	
	private Integer hashKey(Object key) {
		return (int)(((key.hashCode() % Math.pow(2, m)) + Math.pow(2, m)) % Math.pow(2, m));
	}
	
	private Node findSuccessor(Object key) {
		return node.findSuccessor(hashKey(key));
	}
	
	public Node getNode() {
		return node;
	}
	
	public void printMap() {
		node.printMap();
	}

	public void join(DHTNode dhtNode) {
		if (dhtNode != null) {
			this.node.join(dhtNode.node);
		} else {
			this.node.join(null);
		}
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsKey(Object key) {
		return findSuccessor(key).contains(hashKey(key));
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(Object key) {
		try {
			return findSuccessor(key).get(hashKey(key)).get();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Object put(String key, Object value) {
		try {
			return findSuccessor(key).put(hashKey(key), new MarshalledObject<Object>(value));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public MarshalledObject<Object> remove(Object key) {
		return findSuccessor(key).remove(hashKey(key));
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Object> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		throw new UnsupportedOperationException();
	}
}