

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Server extends Thread{
	public String ip;
	public int port;
	public ArrayList<Jobs> jobs;
	public ArrayList<Connections> workers;
	Connections con;
	public boolean alive;
	
	DatagramSocket socket;
	byte[] buf;
	DatagramPacket packet;
	
	DatagramSocket psocket;
	int pport;
	byte[] pbuf;
	DatagramPacket ppacket;
	
	TimerTask task = new TimerTask(){
		public void run() {
			
		}
	};
	
	public Server(String ip, int port) {
		this.ip = ip;
		this.port = port;
		this.alive = true;
		try {
			socket = new DatagramSocket(port);
			buf = new byte[256];
			
			this.pport = port+1;
			psocket = new DatagramSocket(pport);
			pbuf = new byte[256];
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		// check for number of connections
		while (alive) {
			try {
			int size = workers.size();
				for (int i=0; i<size; i++) {
					Timer timer = new Timer();
					timer.schedule(task, 5*1000);
					
					this.con = workers.get(i);
					String str = "ping";
					pbuf = str.getBytes();
					InetAddress address = InetAddress.getByName(con.ip);
					
					packet = new DatagramPacket(pbuf, pbuf.length, address, port);
					psocket.receive(packet);
					
					timer.cancel();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		System.out.println("please enter the IP for the server");
		Scanner in = new Scanner(System.in);
		String ip = new String(in.nextLine());
		System.out.println("please enter port number");
		int port = in.nextInt();
		Server server = new Server(ip, port);
		Runnable runnable = server;
		Thread thread = new Thread(runnable);
		thread.start();
		while(server.alive) {
			// keep listening for UDP packets and divide work and respond accordingly
		}
		in.close();
		thread.join();
	}
}

