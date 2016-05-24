package rsa;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Scanner;

public class Peer {

	public static final String pub = "PUBLIC";
	private int id;

	public Peer(int id) {
		this.id = id;
	}

	public void listen(int port) {
		// Open socket
		ServerSocket listener = null;
		try {
			listener = new ServerSocket(port);

			System.out.println("Waiting for incoming connections...");
			Socket socket = listener.accept();

			String message = "";
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				System.out.println("Waiting for incoming messages...");
				message = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (message.equals(pub)) {
				System.out.println("Responding public key...");
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

	public String request(Socket socket, String jsonReq) {
		String jsonRes = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(jsonReq);
			jsonRes = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonRes;
	}

	public String replay(Socket socket) {
		String jsonReq = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			jsonReq = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonReq;
	}

	public static void main(String[] args) {

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

					System.out.println("Requesting public key ...");
					String m = peer.request(socket, pub);

					PublicKey pubKey = null;
					
					pubKey = (PublicKey) MyBase64.objectFromString(m);

					String ctext = peer.encrypt(message, pubKey);

					socket.close();

					socket = new Socket("127.0.0.1", send_port);

					peer.respond(socket, ctext);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}

	}

}
