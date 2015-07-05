

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class request_client {
	private String ip;
	private int port;
	public boolean status;
	public int id;
	public String s_ip;
	public int ports;
	
	DatagramSocket socket;
	byte[] buf;
	DatagramPacket packet;
	InetAddress address;
	
	public request_client(int port, String ip) throws SocketException, UnknownHostException {
		this.ip = ip;
		this.port = port;
		status = true;
		socket = new DatagramSocket(port);
		address = InetAddress.getLocalHost();
	}
	

	public void send(String message) throws IOException {
		System.out.println("message sending");
		byte[] buf;
		buf = message.getBytes();
		DatagramPacket packet = new DatagramPacket(buf,buf.length, InetAddress.getLocalHost(),this.ports);
		socket.send(packet);
	}

	public void jobs (String ip, int port, String job) throws IOException {
		this.s_ip = ip;
		this.ports = port;
		String message = "requester,new_job,"+this.ip+","+this.port+","+job+",";
		System.out.println(message);
		buf = message.getBytes();
		packet = new DatagramPacket(buf,buf.length, InetAddress.getLocalHost(),port);
		socket.send(packet);
		while(true) {
		socket.receive(packet);
		buf = packet.getData();
		String data = new String(buf, 0, buf.length);
		ArrayList<String> process = new ArrayList<String>(Arrays.asList(data.split(",")));
		if (process.get(1).equals("ping")) {
			message = "requester,ping"+this.id+",";
			this.send(message);
		} else if (process.get(1).equals("complete")) {
			
			//processRequester(process);
			System.out.println("I am complete");
			System.out.println(process.get(2));
			break;
			// this.end();
		} else if (process.get(1).equals("job_ack")) {
			this.id = Integer.parseInt(process.get(2));
		}
		}
	}
	
	public static void main(String[] args) throws IOException {
		// System.out.println("please enter the IP for the server");
		// Scanner in = new Scanner(System.in);
		String ip = new String("localhost");
		// System.out.println("please enter port number");
		int port = Integer.parseInt(args[0]);
		request_client client = new request_client(port, ip);
		// System.out.println("please enter port number for server");
		int sport = Integer.parseInt(args[1]);
		// System.out.println("please enter ip number for server");
		String s_ip = new String("localhost");
		// System.out.println("please enter job for server");
		String job = new String(args[2]);
		client.jobs(s_ip, sport, job);
		// in.close();
	}
}
