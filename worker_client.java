

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class worker_client {
	int id; // will ask server for id
	private String ip;
	private int port;
	private String ips;
	private int ports;
	boolean status;
	
	DatagramPacket packet;
	DatagramSocket socket;
	
	int jobid;
	String from;
	String hash;
	boolean job;
	Passwords pass;
	public String solution = "";

//	char[] pass;
//	int len;
//	char[] list;
	
	public worker_client(String ip, int port, String ip_server, int port_server) throws IOException {
		this.ip  = ip;
		this.port = port;
		ips = ip_server;
		ports = port_server;
		job =  false;
		status = true;
		pass = new Passwords(5, 'a', 'b', "c0b277d6378b9b0f27a33301a137924b", this);
		socket = new DatagramSocket(this.port, InetAddress.getLocalHost());
//		this.len = 5;
//		pass = new char[len];
//		Arrays.fill(pass, 'a');
		
	}
	
	public void Connect() throws IOException {
		System.out.println("connecting with server and getting id");
		byte[] buf;// = new byte[256];
		String message = "worker,new," + this.ip + "," + port + ",";
		String[] myString = message.split(",");
		System.out.println(myString[3]);
		this.send(message);
		// System.out.println("process.get(1)");
// 		buf = message.getBytes();
// //		DatagramPacket packet = null;
// 		packet = new DatagramPacket(buf,buf.length, InetAddress.getByName(this.ip),port);
// 		socket = new DatagramSocket(this.port);
// 		socket.send(packet);
		// System.out.println(".get(1)");
		buf = new byte[256];
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		String data = new String(packet.getData(), 0, packet.getLength());
		// List<String> temp = ; //(ArrayList<String>)Array
		ArrayList<String> process = new ArrayList<String> (Arrays.asList(data.split(",")));
		System.out.println(process.get(1));
		this.id = Integer.parseInt(process.get(1));
	}
	
	public void standBy() {
		while (status) {
			
		}
	}
	
	public int getPort() {
		return this.port;
	}
	
	public void send(String message) throws IOException {
		// System.out.println("message sending by worker");
		// System.out.println(message);
		byte[] buf;
		buf = message.getBytes();
		DatagramPacket packet = new DatagramPacket(buf,buf.length, InetAddress.getLocalHost(),this.ports);
		socket.send(packet);
	}
	
	public void processPacket(ArrayList<String> input) throws IOException{
		if (input.get(1).equals("terminate_job")) {
			// cancel job
			System.out.println("cancel the job somehow");
			this.job = false;
			pass.doJob = false;
		} else {
			System.out.println("hash value");
			this.jobid = Integer.parseInt(input.get(2));
			this.hash = input.get(3);
			System.out.println(input.get(3));
			this.from = input.get(4);
			System.out.println(input.get(4));
			this.job = true;
			doJob();
		}
	}
	
	public void doJob() throws IOException {
		Thread var = new Thread(pass);

		if (this.job) {
			pass.doJob = true;
			pass.setHash(this.hash);
			pass.setStartChar(this.from.charAt(0));
			pass.setEndChar((char)(this.from.charAt(0)+1));
		}

		System.out.println("starting job");

		var.start();
	}

	public void End() throws IOException{
		if (solution.equals("")) {
			// not found
			String message = "worker,reply," +this.jobid+","+this.id+",notfound,";
			this.send(message);
		} else {
			// found
			String message = "worker,reply," +this.jobid+","+this.id+",found,"+solution+",";
			this.send(message);
		}
	}
	
	public static void main(String[] args) throws IOException {
		// System.out.println("what server will i work for?");
		// Scanner scan = new Scanner(System.in);
		// System.out.println("please enter the ip of the server");
		// String ip_server = args[0];
		// System.out.println("port of server");
		int port_server = Integer.parseInt(args[0]);
		// System.out.println("ip of worker");
		// String ip = scan.nextLine();
		// System.out.println("port of wroker");
		int port = Integer.parseInt(args[1]);
		worker_client worker = new worker_client("localhost", port_server, "localhost", port);
		worker.Connect();
		System.out.println("connection complete");
		WorkerComponent listen = new WorkerComponent(worker);
		listen.start();
		worker.standBy();
	}
}
