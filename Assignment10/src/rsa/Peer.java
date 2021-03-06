package rsa;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.Scanner;

public class Peer {

	private static final String PUB = "PUBLIC";
	
	private int id;
	
	private PublicKey otherPubKey;

	public Peer(int id) {
		this.id = id;
		this.otherPubKey = null;
	}

	public void listen(int port) {
		// Open socket
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
			} else {
				System.out.println("\rEncrypted text: " + new String(MyBase64.byteArrayFromString(message)));
				System.out.println("Decrypted text: " + decrypt(message));
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

	// encrypt message
	public String encrypt(String plainText, PublicKey pubKey) {
		return MyBase64.byteArrayToString(Rsa.rsaEncrypt(plainText.getBytes(), pubKey));
	}

	// decrypt message
	public String decrypt(String encryptedText) {
		return new String(Rsa.rsaDecrypt(MyBase64.byteArrayFromString(encryptedText), Rsa.readPrivateKeyFromFile("private" + this.id + ".key")));
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

	public String replay(Socket socket) {
		String message = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			message = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return message;
	}
	
	public PublicKey getOtherPublicKey() {
		return otherPubKey;
	}
	
	public void setOtherPubKey(PublicKey otherPubKey) {
		this.otherPubKey = otherPubKey;
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
						System.out.println("Requesting public key ...");
						String m = peer.request(socket, PUB);
	
						PublicKey pubKey = (PublicKey) MyBase64.objectFromString(m);
						peer.setOtherPubKey(pubKey);
						
						socket.close();
						socket = new Socket("127.0.0.1", send_port);
					}
					
					String ctext = peer.encrypt(message, peer.getOtherPublicKey());
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