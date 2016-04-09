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
import java.util.ArrayList;
import java.util.List;

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
	
	public List<String> masterReplay(DatagramSocket socket) {
		byte[] buffer = new byte[512];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		try {
			socket.receive(packet);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		String received = new String(packet.getData(), 0, packet.getLength());
		
		List<String> returnList = new ArrayList<String>();
		returnList.add(received);
		returnList.add(packet.getAddress().getHostAddress() + ":" + Integer.toString(packet.getPort()));
		
		return returnList;
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
	
	public void masterRespond(DatagramSocket socket, String jsonRes, String recipient, String port) {
		InetAddress recipientAddress = null;
		try {
			recipientAddress = InetAddress.getByName(recipient);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		int recipientPort = Integer.parseInt(port);
		
		DatagramPacket packet = new DatagramPacket(jsonRes.getBytes(), jsonRes.getBytes().length, recipientAddress, recipientPort);
		try {
			socket.setBroadcast(false);
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendBroadcast(DatagramSocket socket, String jsonReq) {
		InetAddress address = null;
		try {
			address = InetAddress.getByName("255.255.255.255");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		DatagramPacket packet = new DatagramPacket(jsonReq.getBytes(), jsonReq.getBytes().length, address, PORT);
		
		try {
			socket.setBroadcast(true);
			socket.send(packet);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String sendBroadcastAndReceivePacket(DatagramSocket socket, String jsonReq) {
		sendBroadcast(socket, jsonReq);
		
		byte[] buffer = new byte[512];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		
		try {
			socket.setSoTimeout(2000);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		
		int count = 3;
		String received = null;
		while (count > 0) {
			try {
				socket.receive(packet);
			} catch(SocketTimeoutException e) {
				count--;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			received = new String(packet.getData(), 0, packet.getLength());
			break;
		}
		
		if (count > 0) {
			return received;
		}
		return null;
	}
	
	public void announceService(DatagramSocket socket, String jsonReq) {
		sendBroadcast(socket, jsonReq);
	}
	
	public String locateService(DatagramSocket socket, String jsonReq) {
		return sendBroadcastAndReceivePacket(socket, jsonReq);
	}
	
	public String requestServiceList(DatagramSocket socket, String jsonReq) {
		return sendBroadcastAndReceivePacket(socket, jsonReq);
	}
}