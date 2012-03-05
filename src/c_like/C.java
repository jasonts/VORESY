package c_like;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class C {
	
	/**
	 * Run a (external) command 
	 * @param command command line
	 * @return run status, 0 is okay.
	 */
	
	public static int System(String command) {
		Process proc;
		int exitVal = 0;
		try {
			proc = Runtime.getRuntime().exec(command);
			//debug purpose start
			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			//String line;
			//while ( (line = br.readLine()) != null) {
			while (br.readLine() != null) {   
	        	
				//System.out.println(line);
				System.out.print("");
			}
			exitVal = proc.waitFor();
			//System.out.println(exitVal);
			//debug purpose end
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return exitVal;
	}
	
	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
