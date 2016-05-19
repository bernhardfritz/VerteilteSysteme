package rmi;

import java.io.Serializable;

public interface Task<T> extends Serializable {
	public T execute();
}