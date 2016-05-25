package hybrid;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Peer {

	private static final String PUB = "PUBLIC";
	private static final String SEC = "SECRET";
	
	private int id;
	
	private PublicKey otherPubKey;
	
	private String secretKey;

	public Peer(int id) {
		this.id = id;
		this.otherPubKey = null;
	}
	
	private static List<Character> characters = new ArrayList<Character>();
	static {
		for(char c = 'a'; c <= 'z'; c++) characters.add(c);
	}
	
	private static String getRandomString(int length) {
		if(length <= 0) return "";
		return characters.get(new Random().nextInt(characters.size())).toString() + getRandomString(length-1);
	}

	public void listen(int port) {
		ServerSocket listener = null;
		try {
			listener = new ServerSocket(port);

			Socket socket = listener.accept();

			String message = "";
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				message = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (message.equals(PUB)) {
				System.out.println("\rResponding public key...");
				PublicKey pubkey = Rsa.readPublicKeyFromFile("public" + this.id + ".key");

				respond(socket, MyBase64.serializeableToString(pubkey));
			} else if (message.startsWith(SEC)) {
				System.out.println("\rResponding secret key...");
				this.secretKey = getRandomString(10);
				respond(socket, encryptRsa(secretKey, (PublicKey) MyBase64.objectFromString(message.substring(SEC.length(), message.length()))));
			} else {
				System.out.println("\rEncrypted text: " + message);
				System.out.println("Decrypted text: " + decryptSecret(message));
				System.out.print("Message: ");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				listener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String encryptRsa(String plainText, PublicKey pubKey) {
		return MyBase64.byteArrayToString(Rsa.rsaEncrypt(plainText.getBytes(), pubKey));
	}

	public String decryptRsa(String encryptedText) {
		return new String(Rsa.rsaDecrypt(MyBase64.byteArrayFromString(encryptedText), Rsa.readPrivateKeyFromFile("private" + this.id + ".key")));
	}
	
	public String encryptSecret(String message) {
		String ctext = "";
		for (int i = 0; i < message.length(); i++) {
			char xor = (char) (message.charAt(i) ^ secretKey.charAt(i % secretKey.length()) + 32);
			ctext = ctext + xor;
		}

		return ctext;
	}
	
	public String decryptSecret(String message) {
		return encryptSecret(message);
	}

	public void respond(Socket socket, String message) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String request(Socket socket, String message) {
		String response = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(message);
			response = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}
	
	public PublicKey getOtherPublicKey() {
		return otherPubKey;
	}
	
	public void setOtherPubKey(PublicKey otherPubKey) {
		this.otherPubKey = otherPubKey;
	}
	
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: java Peer <peer_id> <listen_port> <send_port>");
			System.exit(1);
		}
		
		int id = Integer.parseInt(args[0]);
		int listen_port = Integer.parseInt(args[1]);
		int send_port = Integer.parseInt(args[2]);

		File private_file = new File("private" + id + ".key");
		File public_file = new File("public" + id + ".key");

		if (!private_file.exists() || !public_file.exists()) {
			Rsa.generateKeyPair(id);
		}

		Peer peer = new Peer(id);

		// listen thread
		new Thread() {
			public void run() {
				while (true) {
					peer.listen(listen_port);
				}
			}
		}.start();

		Scanner sc = new Scanner(System.in);
		while (true) {
			System.out.print("Message: ");
			if (sc.hasNext()) {
				Socket socket = null;
				try {
					socket = new Socket("127.0.0.1", send_port);
					String message = sc.nextLine();

					if (peer.getOtherPublicKey() == null) {
						System.out.println("Requesting public key...");
						String m = peer.request(socket, PUB);
						PublicKey pubKey = (PublicKey) MyBase64.objectFromString(m);
						peer.setOtherPubKey(pubKey);
						
						socket.close();
						socket = new Socket("127.0.0.1", send_port);
						
						System.out.println("Requesting secret key...");
						m = peer.request(socket, SEC + MyBase64.serializeableToString(Rsa.readPublicKeyFromFile("public" + id + ".key")));
						String secretKey = peer.decryptRsa(m);
						peer.setSecretKey(secretKey);
						
						socket.close();
						socket = new Socket("127.0.0.1", send_port);
					}

					String ctext = peer.encryptSecret(message);
					peer.respond(socket, ctext);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}