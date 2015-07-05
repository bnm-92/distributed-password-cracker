

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Jobs {
	String hash;
	File log;
	int id;
	ArrayList<Task> tasks;
	boolean complete;
	
	public int totalComplete() {
		int i=0;
		for (i=0; i<tasks.size(); i++) {
			if (tasks.get(i).complete == false) {
				return i;
			}	
		}
		return i;
	}

	public Jobs(String hash, String fileName) {
		this.hash = hash;
		String add = ".txt";
		log = new File("/"+fileName+add);
		tasks = this.assignTask();
		complete = false;
	}
	
	public ArrayList<Task> assignTask() {
		String s = new String("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
		ArrayList<Task> temp = new ArrayList<Task>();
		for (int i=0; i<62; i++) {
			Task task = new Task();
			char chr = s.charAt(i);
			char[] from = new char[5]; 
			Arrays.fill(from,chr);
			task.from = new String(from);
			task.complete = false;
			task.taken = false;
			temp.add(task);
		}
		return temp;
	}
	
	public String findTask() {
		String temp;
		for (Task t : tasks) {
			if (t.taken == false) {
				t.taken = true;
				temp = t.from;
				return temp;
			}
		}

		for (Task t : tasks) {
			if (t.complete == false) {
				t.taken = true;
				temp = t.from;
				return temp;
			}
		}

		return null;
		
	}
	
	public void  writeToLog(String input) {
		try{
			FileWriter fw = new FileWriter(log.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(input+"\n");
			bw.close();
			System.out.println("written");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public class Task {
		String from;
		boolean taken;
		boolean complete;
	}
	
}
