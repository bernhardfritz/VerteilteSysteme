package secret;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Peer {

	private String key;

	public Peer(String key) {
		this.key = key;
	}

	public void listen(int port) {
		// Open socket
		ServerSocket listener = null;
		try {
			listener = new ServerSocket(port);

			Socket socket = listener.accept();

			String ctext = "";
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				ctext = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("\rEncrypted text: " + ctext);

			System.out.println("Decrypted text: " + decrypt(ctext));

			System.out.print("Message: ");
			
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
	public String encrypt(String message) {

		char xor;
		String ctext = "";
		for (int i = 0; i < message.length(); i++) {
			xor = (char) (message.charAt(i) ^ key.charAt(i % key.length()));
			ctext = ctext + xor;
		}

		return ctext;

	}

	// decrypt message
	public String decrypt(String message) {

		return encrypt(message);

	}

	public void respond(Socket socket, String message) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		String key = args[0];
		int listen_port = Integer.parseInt(args[1]);
		int send_port = Integer.parseInt(args[2]);

		Peer peer = new Peer(key);

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

					String ctext = peer.encrypt(message);

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
