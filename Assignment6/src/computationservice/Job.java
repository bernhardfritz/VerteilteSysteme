package computationservice;

import java.io.Serializable;

public interface Job<T> extends Serializable {

		public boolean isDone();
		
		public void setResult(Object result);
		
		public T getResult();
}