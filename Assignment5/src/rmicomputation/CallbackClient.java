package rmicomputation;

import java.io.Serializable;
import java.rmi.RemoteException;

public class CallbackClient implements Callback, Serializable {

	private static final long serialVersionUID = -3049793094328358180L;
	
	private boolean shutdown;
	
	private String answer;
	
	public CallbackClient() {
		this.shutdown = false;
	}

	@Override
	public void answer(String answer) throws RemoteException {
		this.answer = answer;
		this.shutdown = true;
	}

	@Override
	public boolean isShutdown() throws RemoteException {
		return shutdown;
	}

	@Override
	public void setShutdown(boolean shutdown) throws RemoteException {
		this.shutdown = shutdown;
	}

	@Override
	public String getAnswer() throws RemoteException {
		return answer;
	}
}