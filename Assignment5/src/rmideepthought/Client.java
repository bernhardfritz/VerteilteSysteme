package rmideepthought;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Client implements Serializable, ICallback{
	
	private boolean shutdown = false;
	private String answer;
	
	public boolean isShutdown() {
		return shutdown;
	}

	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}
	
	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public static void main(String[] args) {
		try {
			
			//create callback client
		    ICallback callback = new Client();
		    
		    //export callback client
		    UnicastRemoteObject.exportObject(callback, 0);
		    
		    Registry registry = LocateRegistry.getRegistry();

		    RemoteServer stub = (RemoteServer) registry.lookup("Server");
		   
		    String question = "What is the best number?";
		    
		    //rmi call
		    stub.deepthought(question, callback);
		    
		    System.out.println("Question sent:");
		    System.out.println(question);
		    
		    //wait for callback
		    while(!callback.isShutdown()){
		    	Thread.sleep(500);
		    }
		    
		    //print answer
		    System.out.println(callback.getAnswer());
		    
		    //unexport object
		    UnicastRemoteObject.unexportObject(callback, true);
		    
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

	@Override
	public void answer(String answer) throws RemoteException {
		this.answer = answer;
		this.shutdown = true;
	}
}