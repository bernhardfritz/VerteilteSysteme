package webservice;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class Client {
	public static void main(String[] args) throws MalformedURLException {
		URL url = new URL("http://localhost:8080/webservice/server?wsdl");
		QName qname = new QName("http://webservice/", "ServerService");
		
		Service service = Service.create(url, qname);
		IServer server = service.getPort(IServer.class);
		
		Random r = new Random(0);
		long start = System.currentTimeMillis();
		for(int i = 0; i < 1000; i++) {
			server.addition(BigInteger.valueOf(r.nextLong()), BigInteger.valueOf(r.nextLong()));
		}
		long end = System.currentTimeMillis();
		double duration = (end - start) / 1000.0; // in ms
		System.out.println("[WebService] Duration: " + duration + " ms");
		
		server.shutdown();
	}
}