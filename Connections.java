

public class Connections {
	public String ip;
	public int port;
	public int id;
	public boolean status;
	public boolean free;
	public int ping_count = 0;
	public int jobid;
	
	public Connections (String ip, int port, int id, boolean status) {
		this.ip = ip;
		this.port = port;
		this.id = id;
		this.status = status;
		this.free = true;
	}
}
