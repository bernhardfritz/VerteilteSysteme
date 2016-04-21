package rmideepthought;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICallback extends Remote {

	public void answer(String answer) throws RemoteException;
	
	public boolean isShutdown() throws RemoteException;

	public void setShutdown(boolean shutdown) throws RemoteException;
	
	public String getAnswer() throws RemoteException;

	public void setAnswer(String answer) throws RemoteException;
	
}
