package locatingservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Protocol {
	public final int PORT = 9090;
	
	// sends request jsonReq and returns response jsonRes to caller
	public String request(Socket socket, String jsonReq) {
		String jsonRes = "";
		try {
			// use input / output streams to realize the client side of the communication
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(jsonReq);
			jsonRes = in.readLine();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return jsonRes;
	}
	
	// waits for incoming request and returns content to caller
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
	
	public String masterReplay(DatagramSocket socket) {
		byte[] buffer = new byte[512];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		String received = "";
		try {
			socket.receive(packet);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		received = new String(packet.getData(), 0, packet.getLength());
		
		return received;
	}
	
	// sends response jsonRes and returns to caller
	public void respond(Socket socket, String jsonRes) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(jsonRes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void announceService(DatagramSocket socket, String jsonReq) {
		InetAddress address = null;
		try {
			address = InetAddress.getByName("255.255.255.255");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		DatagramPacket packet = new DatagramPacket(jsonReq.getBytes(), jsonReq.getBytes().length, address, socket.getPort());
		
		try {
			socket.setBroadcast(true);
			socket.send(packet);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String locateService(DatagramSocket socket, String jsonReq) {
		announceService(socket, jsonReq);
		
		byte[] buffer = new byte[512];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		
		try {
			socket.setSoTimeout(3000);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int count = 3;
		String received = null;
		while (count > 0) {
			try {
				socket.receive(packet);
				if (packet.getAddress().getHostAddress().equals("255.255.255.255")) {
					continue;
				}
			} catch(SocketTimeoutException e) {
				count--;
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			received = new String(packet.getData(), 0, packet.getLength());
		}
		
		if (count > 0) {
			return received;
		}
		return null;
	}
	
	public String requestServiceList(DatagramSocket socket, String jsonReq) {
		return locateService(socket, jsonReq);
	}
}