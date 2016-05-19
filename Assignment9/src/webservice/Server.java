package webservice;
import java.math.BigInteger;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

@WebService(endpointInterface = ("webservice.IServer"))
public class Server implements IServer {
	
	static Endpoint endpoint;

	@Override
	public BigInteger addition(BigInteger a, BigInteger b) {
		return a.add(b);
	}

	@Override
	public BigInteger subtraction(BigInteger a, BigInteger b) {
		return a.subtract(b);
	}

	@Override
	public BigInteger multiplication(BigInteger a, BigInteger b) {
		return a.multiply(b);
	}

	@Override
	public BigInteger lucas(BigInteger a) {
		if(a.equals(BigInteger.ZERO)) return BigInteger.valueOf(2L);
		if(a.equals(BigInteger.ONE)) return BigInteger.ONE;
		else return lucas(a.subtract(BigInteger.ONE)).add(lucas(a.subtract(BigInteger.valueOf(2))));
	}
	
	@Override
	public void shutdown() {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				endpoint.stop();
			}
		}.start();
	}
	
	public static void main(String[] args) {
		IServer server = new Server();
		endpoint = Endpoint.create(server);
		endpoint.publish("http://localhost:8080/webservice/server");
	}
}