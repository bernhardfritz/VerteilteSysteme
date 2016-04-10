package locatingservices;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

public class RequestHandler implements Runnable {
	private long threadID;
	private Protocol protocol;
	private Socket socket;
	private List<Service> availabeServices;
	
	public RequestHandler(long threadID, Protocol protocol, Socket socket, List<Service> availabeServices){
		this.threadID = threadID;
		this.protocol = protocol;
		this.socket = socket;
		this.availabeServices = availabeServices;
	}
	
	@Override
	public void run() {
		Gson gson = new Gson();

        try {
			// Receive request using protocol
			String jsonOpReq = protocol.replay(socket);
			// Unwrap JSON request
			OperationRequest opReq = gson.fromJson(jsonOpReq, OperationRequest.class);
			System.out.printf("%d: Client requesting result of %s %s\n", threadID, opReq.getOperation(), Arrays.toString(opReq.getOperators()));
			
			Integer[] operators = new Integer[opReq.getOperators().length];
			for (int i = 0; i < opReq.getOperators().length; i++) {
				operators[i] = Integer.valueOf(opReq.getOperators()[i]);
			}
			
			System.out.println(threadID + ": Calculating request...");
			int result = 0;
			for (Service<Integer> service : availabeServices) {
				if (service.getName().equals(opReq.getOperation())) {
					result = service.execute(operators);
					break;
				}
			}
			System.out.printf("%d: Returning result(%d).\n", threadID, result);
			
			// Create JSON operation response
			OperationResponse opRes = new OperationResponse(result);
			String jsonOpRes = gson.toJson(opRes);
			// Send response using protocol
			protocol.respond(socket, jsonOpRes);
        } finally {
        	System.out.println(threadID + ": Closing connection...");
            try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
}