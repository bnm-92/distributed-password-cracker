

public class Ping implements Runnable {
	String ip;
	int port;
	boolean alive;
	
	public Ping(String ip, int port) {
		this.ip = ip;
		this.port = port;
		alive = true;
	}
	
	public void run() {
		while (alive) {
			//keep pinging
		}
	}
	
}
