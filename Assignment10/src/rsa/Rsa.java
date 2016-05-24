package rsa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

public class Rsa {

	public static void generateKeyPair(int peer) {

		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

			kpg.initialize(2048);
			KeyPair kp = kpg.genKeyPair();

			KeyFactory fact = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
			RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);

			saveToFile("public" + peer + ".key", pub.getModulus(), pub.getPublicExponent());
			saveToFile("private" + peer + ".key", priv.getModulus(), priv.getPrivateExponent());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void saveToFile(String fileName, BigInteger mod, BigInteger exp) throws IOException {
		ObjectOutputStream oout = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		try {
			oout.writeObject(mod);
			oout.writeObject(exp);
		} catch (Exception e) {
			throw new IOException("Unexpected error", e);
		} finally {
			oout.close();
		}
	}

	public static byte[] rsaEncrypt(byte[] data, PublicKey pubKey) {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			byte[] cipherData = cipher.doFinal(data);
			return cipherData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static byte[] rsaDecrypt(byte[] data, PrivateKey privKey) {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			byte[] cipherData = cipher.doFinal(data);
			return cipherData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static PublicKey readPublicKeyFromFile(String keyFileName) {
		ObjectInputStream oin = null;
		try {
			InputStream in = new FileInputStream(keyFileName);
			oin = new ObjectInputStream(new BufferedInputStream(in));
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PublicKey pubKey = fact.generatePublic(keySpec);
			return pubKey;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				oin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static PrivateKey readPrivateKeyFromFile(String keyFileName) {
		ObjectInputStream oin = null;
		try {
			InputStream in = new FileInputStream(keyFileName);
			oin = new ObjectInputStream(new BufferedInputStream(in));
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PrivateKey privKey = fact.generatePrivate(keySpec);
			return privKey;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				oin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		String plainText = "asdffa;skldjfa;slkfjda;lkjfa";
		String encryptedText = MyBase64.byteArrayToString(rsaEncrypt(plainText.getBytes(), (PublicKey) MyBase64.objectFromString(MyBase64.serializeableToString(readPublicKeyFromFile("public0.key")))));
		String decryptedText = new String(rsaDecrypt(MyBase64.byteArrayFromString(encryptedText), readPrivateKeyFromFile("private0.key")));
		System.out.println(plainText);
		System.out.println(encryptedText);
		System.out.println(decryptedText);
	}
}
