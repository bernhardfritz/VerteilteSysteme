package rsa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

public class MyBase64 {

	public static String serializeableToString(Serializable o) {
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream( baos );
			oos.writeObject( o );
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
	
	public static String byteArrayToString(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}
	
	public static Object objectFromString(String s) {
		ObjectInputStream ois = null;
		Object o = null;
		try {
			byte[] data = Base64.getDecoder().decode(s);
			ois = new ObjectInputStream(new ByteArrayInputStream(data));
			o = ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return o;
	} 
	
	public static byte[] byteArrayFromString(String s) {
		return Base64.getDecoder().decode(s);
	}
	
	public static void main(String[] args) {
		String plainText = "test";
		String encodedText = MyBase64.byteArrayToString(plainText.getBytes());
		String decodedText = new String((byte[]) MyBase64.byteArrayFromString(encodedText));
		System.out.println(plainText);
		System.out.println(encodedText);
		System.out.println(decodedText);
	}
}
