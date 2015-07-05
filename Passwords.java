

import java.util.Arrays;

public class Passwords implements Runnable{
	char[] pass;
	int len;
	char[] list;
	char startChar;
	char endChar;
	String hash;
	public boolean doJob;
	worker_client worker;
	
	public Passwords(int len, char startChar, char endChar, String hash, worker_client worker) {
		this.len = len;
		pass = new char[len];
		Arrays.fill(pass, startChar);
		this.startChar = startChar;
		this.endChar = endChar;
		this.hash = hash;
		this.makeList();
		this.worker = worker;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public void setEndChar(char endChar) {
		this.endChar = endChar;
	}
	
	public void setStartChar(char startChar) {
		Arrays.fill(pass, 'a');
		this.startChar = startChar;
		this.pass[0] = this.startChar;
//		Arrays.fill(pass, 'a');
	}
	
	public void makeList() {
		list = new char[63];
		String s = new String("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789?");
		for (int i=0; i<63; i++) {
			list[i] = s.charAt(i); 
		}
	}
	
	public char next(char in) {
		for (int i=0; i<63; i++) {
			if (list[i] == in) {
				i++;
				return list[i%63];
			}
		}
		return 'a';
	}
	
	public boolean checkForHash( char[] password){
		try{
			String input = new String(password);
			java.security.MessageDigest digest = null;
			digest = java.security.MessageDigest.getInstance("MD5");
			digest.reset();
			digest.update(input.getBytes("UTF-8"));
			StringBuffer sb = new StringBuffer();
			byte[] result = digest.digest();
			for (int i = 0; i < result.length; i++) {
		          sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
		        }
			String hash = sb.toString();
			if (hash.equals(this.hash)) {
				System.out.println("found the hash");
				return true;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	public void generate (){
		try{
			do {
				// System.out.println(this.pass);
				if (doJob) {
				if (checkForHash(this.pass)) {
						// return this.pass;
					worker.solution = new String(this.pass);
					worker.End();
				}
				
				int index = this.len - 1;
				while(index >= 0) {
					this.pass[index] = next(pass[index]);
					
					if (pass[index] == '?') {
						if (index > 0) {
							this.pass[index] = 'a';
						}
						index--;
					} else {
						break;
					}
				}
				} else {
					System.out.println("quit the job");
					worker.solution = "";
					return;
				}
			} while (pass[0] != endChar);
			
			//
//			bw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		worker.solution = "";
		// return null;
	}

	public void run() {
		generate();
	}

//	public static void main(String[] args) {
//		Passwords pass = new Passwords(5, 'a', 'b', "c0b277d6378b9b0f27a33301a137924b");
//		pass.makeList();
//		System.out.println(String.valueOf(14));
////		pass.generate();
//		char[] in = new char[5];
////		caH3h
//		in[0] = 'c';in[1] = 'a';in[2] = 'H';in[3] = '3';in[4] = 'h';
//		System.out.println(in);
////		System.out.println(pass.checkForHash(in));
//		pass.setStartChar('c');
//		pass.setEndChar('d');
//		pass.generate();
//		
////		pass.generateHash();
//	}
}
