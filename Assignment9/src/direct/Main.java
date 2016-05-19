package direct;

import java.math.BigInteger;
import java.util.Random;

public class Main {
	
	public static BigInteger addition(BigInteger a, BigInteger b) {
		return a.add(b);
	}
	
	public static void main(String[] args) {
		Random r = new Random(0);
		long start = System.currentTimeMillis();
		for(int i = 0; i < 1000; i++) {
			addition(BigInteger.valueOf(r.nextLong()), BigInteger.valueOf(r.nextLong()));
		}
		long end = System.currentTimeMillis();
		double duration = (end - start) / 1000.0; // in ms
		System.out.println("[Direct] Duration: " + duration + " ms");
	}
}