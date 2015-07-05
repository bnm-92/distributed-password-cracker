import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
// import java.util.concurrent.TimeUnit;
// import Task.java;

public class TheServer implements java.io.Serializable {
	
	public static TheServer server;
	
	private String ip;
	private int port;
	public ArrayList<Jobs> jobs = new ArrayList<Jobs>(); // jobs from requesters
	public ArrayList<Connections> workers = new ArrayList<Connections>();
	public ArrayList<Connections> requesters = new ArrayList<Connections>();
	private int id = 0;
	public boolean status;
	
	DatagramSocket socket;
	byte[] buf;
	DatagramPacket packet;
	InetAddress address;
	String [] myString;
	
	boolean startJob;

	public TheServer(String ip, int port) throws SocketException, UnknownHostException {
		this.ip = ip;
		this.port = port;
		status = true;
		socket = new DatagramSocket(port);
		address = InetAddress.getLocalHost();
		startJob = true;
	}
	
	public String getIp() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}

	public void processPacket(DatagramPacket packet) throws IOException {
		int rport = packet.getPort();
		InetAddress address = packet.getAddress();
		byte[] buf = packet.getData();
		String data = new String(buf, 0, buf.length);
		System.out.println(data);
		ArrayList<String> process = new ArrayList<String>(Arrays.asList(data.split(",")));
		myString = data.split(",");
		if (process.get(0).equals("worker")) {
			processWorker(process);
		} else if (process.get(0).equals("requester")) {
			processRequester(process);
		}	
	}
	
	public void processWorker(ArrayList<String> input) throws IOException {
		if (input.get(1).equals("new")) {
			System.out.println("new worker received");
			// add connection
			String ip = input.get(2);
			System.out.print(input.get(3));
			System.out.print("clear");
			// int port = Integer.parseInt(input.get(3));
			int port = Integer.parseInt(myString[3]);
			int id = this.id++;
			Connections conn =  new Connections(ip, port, id, true);
			this.workers.add(conn);
			// send a reply to worker with his id
			String message = "server,"+id+",standby,";
			this.send(ip, port, message);
			if (jobs.size() > 0) {
				startJob();
			}

			
		} else if (input.get(1).equals("ping")) {
			// returned ping
			int id = Integer.parseInt(input.get(2));
			for (int i=0; i<workers.size(); i++ ) {
				if (workers.get(i).id == id) {
					workers.get(i).ping_count = 0;
					break;
				}
			}
		} else if (input.get(1).equals("reply")){
			// replied from a job

			System.out.println("new worker reply recieved");
			int idJ = Integer.parseInt(input.get(2));
			int idW = Integer.parseInt(input.get(3));
			for (Connections conn : workers) {
				if (idW == conn.id) {
					conn.free = true;
				}
			}

			if (input.get(4).equals("found")) {
				System.out.println("pasword found");
				for (int i=0; i<jobs.size(); i++) {
					if (idJ == jobs.get(i).id) {
						jobs.get(i).writeToLog("found,"+input.get(5));
						finishJob(idJ, input.get(5));
						break;
					}
				}
			} else {
				for (int i=0; i<jobs.size(); i++) {
					if (idJ == jobs.get(i).id) {
//						jobs.get(i).writeToLog("done,"+input.get(4)+","+input.get(5));
						taskComplete(idJ,idW,input.get(4), input.get(5));
						nextTask(idJ, idW);
						break;
					}
				}
			}
			
		} else if (input.get(1).equals("ack")) {
			// job ack
			int idJ = Integer.parseInt(input.get(2));
			int idW = Integer.parseInt(input.get(3));
			System.out.println("job ack from, wworker id" + idJ + " and job id: "+ idW);
		}
	}
	
	public void startJob() throws IOException{
		// if (startJob) {
		startJob = false;	
		int job = getJob();
		solveJob(job);
		// }
	}

	public void finishJob(int idJ, String password) throws IOException {
		// cancel all tasks with workers with idJ
		// send requester the password with idJ
		startJob = true;

		int ii=0;
		for (Jobs j : this.jobs) {
			if (j.id == idJ) {
				// j.complete = true;
				jobs.remove(ii);
				break;
			}
			ii++;
		}

		if (jobs.size() == 0) {
			startJob = true;
		}
		
		for (int i=0; i<requesters.size(); i++) {
			if (requesters.get(i).jobid == idJ ) {
				System.out.println(password + " this was the passowrd");
				send(requesters.get(i).ip, requesters.get(i).port,"server,complete,"+password+",");
			}
		}
		
		// you can store the password and hash if you want if the requester is down
		
		for (int i=0; i<workers.size(); i++) {
			if (workers.get(i).jobid == idJ) {
				send(workers.get(i).ip,workers.get(i).port, "server,terminate_job,");
				workers.get(i).free = true;
			}
		}

		if (jobs.size() > 0) {
			startJob();
		}

	}
	
	public void taskComplete(int idJ,int idW, String from, String till) throws IOException {
		for (int i=0; i<jobs.size(); i++) {
			if (idJ == jobs.get(i).id) {
				jobs.get(i).writeToLog("done,"+from+","+till+",");
//				jobs.get(i).update(till);
				for (Jobs.Task t : jobs.get(i).tasks) {
					if (t.from.equals(from)) {
						t.complete = true;
					}
				}
				// nextTask(idJ, idW);
				break;
			}
		}
	}
	
	public void nextTask(int idJ, int idW) throws IOException {
		for (int i=0; i< workers.size(); i++) {
			if (workers.get(i).id == idW) {
				for (int j=0; j<jobs.size();j++) {
					if (jobs.get(j).id == idJ && !jobs.get(j).complete) {
//						String from = jobs.get(j).current;
						String from = jobs.get(j).findTask();
						String task = "server,job,"+idJ+","+jobs.get(j).hash+","+from+",";
						send(workers.get(i).ip, workers.get(i).port, task);
						break;
					}
				}
				break;
			}
		}
	}
	
	public void processRequester(ArrayList<String> input) throws IOException {
		if (input.get(1).equals("new_job")) {
			System.out.println("new job recieved");
			String hash = input.get(4);
			String ip = input.get(2);
			int port = Integer.parseInt(input.get(3));
			newJob(hash, ip, port);
		} else if (input.get(1).equals("ping")) {
			int id = Integer.parseInt(input.get(2));
			for (int i=0; i<requesters.size(); i++ ) {
				if (requesters.get(i).id == id) {
					requesters.get(i).ping_count = 0;
					break;
				}
			}
		}
	}
	
	public void newJob(String hash, String ip, int port) throws IOException{
		System.out.println("new job is being created");
		Connections conn = new Connections(ip, port, this.id++, true);
		Jobs job = new Jobs(hash, new String(""+this.id+""));
		String message = "server,job_ack,"+this.id+",standby,";
		this.send(ip,port,message);
		requesters.add(conn);
		jobs.add(job);
		startJob();
		System.out.println("new job created");
	}
	
	public void send(String ip, int port, String message) throws IOException {
		buf = message.getBytes();
		System.out.println(message);
		packet = new DatagramPacket(buf,buf.length, InetAddress.getLocalHost(),port);
		socket.send(packet);
		System.out.println("message sent by server");
	}
	
	int getJob() {
		for (int i=0; i<jobs.size();i++) {
			if (!jobs.get(i).complete) {
				return i;
			}
		}
		// System.out.println("no jobs");
		return -1;
	}

	public void solveJob(int jj) throws IOException {
		if (workers.size() > 0) {
			for (Connections conn: workers) {
				if (conn.free) {
					String from = jobs.get(jj).findTask();
					if (from != null) {
						conn.free = false;
						System.out.println("job sending");
						String task = "server,job,"+jobs.get(jj).id+","+jobs.get(jj).hash+","+from+",";
						send(conn.ip, conn.port, task);
					}
				}
			}
		}
	}

	public String  getJobState() {
		String job = "";
		for (int i=0; i<jobs.size(); i++) {
			String tmp = new String("" +this.jobs.get(i).hash+ "," +this.jobs.get(i).id +"," +this.jobs.get(i).totalComplete()+",");
			job = job + tmp;  
		}
		return job;
	}

	public void checkJobs() throws IOException {
		if (jobs.size() > 0) {
			startJob();
		}
	}

	public String getWorkerState() {
		String worker = "";
		for (int i=0; i<workers.size(); i++) {
			String tmp = new String(""+this.workers.get(i).id +","+this.workers.get(i).jobid+"," +this.workers.get(i).ip +"," + this.workers.get(i).port+ ",");
			worker = worker + tmp;  	
		}
		return worker;
	}

	public String getRequestersState() {
		String requesters = "";
		for (int i=0; i<this.requesters.size(); i++) {
			String tmp = new String("" +this.requesters.get(i).id+","+ this.requesters.get(i).jobid + "," +this.requesters.get(i).ip+ "," +this.requesters.get(i).port+",");
			requesters = requesters + tmp;  	
		}
		return requesters;
	}



	public void noteState(File log) throws IOException{
		String jobs = getJobState();
		String workers = getWorkerState();
		String requesters = getRequestersState();
		String content = new String(""+this.ip+","+this.port+",");
		content = content + "\n" + jobs + "\n" + workers + "\n" + requesters+ "\n";
		FileInputStream fis = null;
		log.createNewFile();
		FileWriter fw = new FileWriter(log.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.close();
	}

	public void mainloop() throws IOException {
		this.checkJobs();
		ScheduledThreadPoolExecutor p = new ScheduledThreadPoolExecutor(1);
		p.scheduleAtFixedRate(new Runnable(){
			public void run() {
				try {
					System.out.println("logging state");
					File log = new File("log.txt");

					noteState(log);

      //           		// Write to disk with FileOutputStream
						// FileOutputStream f_out = new 
						// FileOutputStream("myobject.data");

						// // Write object with ObjectOutputStream
						// ObjectOutputStream obj_out = new
						// ObjectOutputStream (f_out);

						// // Write object out to disk
						// obj_out.writeObject ( TheServer.server );
				} catch (IOException e) {
					e.printStackTrace();
				}


			}   
            }, 0, 20, TimeUnit.SECONDS); // execute every 10 seconds
	}
	
	public static void main (String [] args) throws IOException, InterruptedException, ClassNotFoundException {

		try {
				// Read from disk using FileInputStream
			File f = new File("log.txt");
			if (f.exists()) {
				TheServer server = new TheServer("localhost", Integer.parseInt(args[0])+1);
				BufferedReader br = new BufferedReader(new FileReader(f));
				String content;

				if ((content = br.readLine()) != null)  {
					// convert to char and display it
					String data = new String((content));
					ArrayList<String> data2 = new ArrayList<String> (Arrays.asList(data.split(",")));
					TheServer.server = new TheServer(data2.get(0), Integer.parseInt(data2.get(1)));//.server;
					server = TheServer.server;
				}
					//jobs
				// content = fis.read();
				if ((content = br.readLine()) != null) {
					// convert to char and display it
					if (content.equals("")) {

					} else {
					String data = new String((content));
					ArrayList<String> data2 = new ArrayList<String> (Arrays.asList(data.split(",")));
					for (int i=0; i<data2.size(); i=i+3) {
						String hash = data2.get(i);
						int id = Integer.parseInt(data2.get(i+1));
						int complete = Integer.parseInt(data2.get(i+2));
						Jobs job = new Jobs(hash, new String(""+id+""));
						for (int j=0; j<complete; j++) {
							job.tasks.get(j).complete = true;
						}
						TheServer.server.jobs.add(job);
					}
				}
					// TheServer.server = new TheServer(data2.get(0), Integer.parseInt(data2.get(1)));//.server;
				}
				// workers
				// content = fis.read();
				if ((content = br.readLine()) != null || !content.equals("")) {
					// convert to char and display it
					if (content.equals("")) {

					} else {
					String data = new String((content));
					ArrayList<String> data2 = new ArrayList<String> (Arrays.asList(data.split(",")));
					for (int i=0; i< data2.size(); i=i+4) {
						boolean status = true;
						int id = Integer.parseInt(data2.get(i));
						int jobid = Integer.parseInt(data2.get(i+1));
						String ip = (data2.get(i+2));
						int port = Integer.parseInt(data2.get(i+3));
						Connections con = new Connections(ip,port,id,status);
						con.jobid = jobid;
						TheServer.server.workers.add(con);
					}
					// TheServer.server = new TheServer(data2.get(0), Integer.parseInt(data2.get(1)));//.server;
				}
			}
				// requesters
				// content = fis.read();
				if ((content = br.readLine()) != null || !content.equals("")) {
					// convert to char and display it
					if (content.equals("")) {

					} else {
					String data = new String((content));
					ArrayList<String> data2 = new ArrayList<String> (Arrays.asList(data.split(",")));
					for (int i=0; i< data2.size(); i=i+4) {
						boolean status = true;
						int id = Integer.parseInt(data2.get(i));
						int jobid = Integer.parseInt(data2.get(i+1));
						String ip = (data2.get(i+2));
						int port = Integer.parseInt(data2.get(i+3));
						Connections con = new Connections(ip,port,id,status);
						con.jobid = jobid;
						TheServer.server.requesters.add(con);
					}
				}
			}
					// 	FileInputStream f_in = new FileInputStream("myobject.data");
					// 	// Read object using ObjectInputStream
					// 	ObjectInputStream obj_in = new ObjectInputStream (f_in);

					// 	// Read an object
					// 	Object obj = obj_in.readObject();
					// if (obj instanceof TheServer)
					// {
					// 	TheServer.server = (TheServer) obj;
				ServerComponent listen = new ServerComponent("listen");
				ServerComponent ping = new ServerComponent("ping");						
				ping.start();
				listen.start();
				server.mainloop();
					// }
			} else {
					// System.out.println("please enter the IP for the server");
					// Scanner in = new Scanner(System.in);
				String ip = new String("localhost");
					// System.out.println("please enter port number");
				int port = Integer.parseInt(args[0]);
				TheServer.server = new TheServer(ip, port);
			//		server.setVariable();
					// System.out.println("thread");
				ServerComponent listen = new ServerComponent("listen");
					// System.out.println("listen");
				ServerComponent ping = new ServerComponent("ping");
					// System.out.println("ping");
				ping.start();
				listen.start();
				System.out.println("start");
				server.mainloop();
				System.out.println("end");
				ping.join();
				listen.join();			
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}


