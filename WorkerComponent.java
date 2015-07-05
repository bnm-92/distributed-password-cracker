

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Timer;

// import Jobs.Task;


public class WorkerComponent extends Thread {
	
	boolean listen = false;

	public worker_client worker;
	
	public WorkerComponent(worker_client worker) {
		this.worker = worker;
		listen = true;
	}

	public void listen() throws Exception {
		int port = worker.getPort();
		DatagramSocket socket = worker.socket;
		byte[] buf = new byte[256];
//		InetAddress address = InetAddress.getByName("localhost");
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
					
		while (listen) {
			System.out.println("in listen");
			socket.receive(packet);
			System.out.println("in listen2");
			byte[] buffer = packet.getData();
			String data = new String(buf, 0, buf.length);
			ArrayList<String> process = new ArrayList<String>(Arrays.asList(data.split(",")));
			if (process.get(1).equals("ping")) {
				worker.send("worker,ping,"+worker.id+",");
			} else if (process.get(1).equals("terminate_job")) {
				System.out.println("terminate_job ");
				worker.pass.doJob = false;
			}else {
				System.out.println("packet recieved");
				worker.processPacket(process);
			}
		}
		socket.close();
		}
		

		
	public void run() {
		if (listen) {
			try {
				listen();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
	
}
