

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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// import password.Jobs.Task;


public class ServerComponent extends Thread {
	
	boolean listen = false;
	boolean send = false;
	boolean ping = false;
	public TheServer server = TheServer.server;
	
	public ServerComponent(String instruction) {
		if (instruction.equals("listen")) {
			listen = true;
			System.out.println("listen true");
		} else if (instruction.equals("send")) {
			send = true;
		} else if (instruction.equals("ping")) {
			ping = true;
			System.out.println("ping true");
		}
	}

	public void listen() throws Exception {
		int port = server.getPort();
		DatagramSocket socket = server.socket;
		byte[] buf = new byte[256];
//		InetAddress address = InetAddress.getByName("localhost");
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
					
		while (listen) {
			socket.receive(packet);
			server.processPacket(packet);
		}
		socket.close();
	}
	
	public void ping() throws IOException {
		System.out.println("in ping");
		

            ScheduledThreadPoolExecutor p = new ScheduledThreadPoolExecutor(1);
            p.scheduleAtFixedRate(new Runnable(){
                public void run() {
                	try {

    				int size = server.workers.size();
			// System.out.println(size);
				

				for (int i=0; i<size; i++) {
					// Connections conn = server.workers.get(i);
					String str = "server,ping,";
					byte[] buf = str.getBytes();
					System.out.println("pinging on going");
					if (server.workers.get(i).ping_count < 10) {
						server.workers.get(i).ping_count++;
						DatagramPacket packet = new DatagramPacket(buf,buf.length, InetAddress.getLocalHost(),server.workers.get(i).port);
						System.out.println("here");
						server.socket.send(packet);
					} else {
						// conn.status = false;
						server.workers.remove(i);
					}    						
                }
                	} catch (IOException e) {
                		e.printStackTrace();
                	}
    				

             }   
            }, 0, 5, TimeUnit.SECONDS); // execute every 5 seconds
        }
	
	public void run() {
		try {
			if (listen) {
			try {
				listen();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (send) {
			
		} else if (ping) {
			ping();
		}

		} catch(IOException e) {
			e.printStackTrace();
		}
	}
} 
