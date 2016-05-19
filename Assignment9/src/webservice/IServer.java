package webservice;
import java.math.BigInteger;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface IServer {
	@WebMethod public BigInteger addition(BigInteger a, BigInteger b);
	@WebMethod public BigInteger subtraction(BigInteger a, BigInteger b);
	@WebMethod public BigInteger multiplication(BigInteger a, BigInteger b);
	@WebMethod public BigInteger lucas(BigInteger a);
	@WebMethod public void shutdown();
}
