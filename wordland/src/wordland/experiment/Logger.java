package wordland.experiment;

import java.io.*;

public class Logger {
	private String fn;
	private PrintWriter pw=null;
	private int i=0;
	public Logger(String n) {
		fn=n;
	}
	public void begin() {
		try {
			pw=new PrintWriter(fn+i+".txt");
			i++;
		}
		catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}
	}
	public void println(String s) {
		pw.println(s);
	}
	public void close() {
		pw.close();
	}
} 