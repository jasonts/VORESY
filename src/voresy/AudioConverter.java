package voresy;
import c_like.C;

public class AudioConverter implements Runnable {
	
	/**
	 * AudioConverter Constructor<br>
	 * This constructor is used for VORESY system.<br>
	 * the output file has set to "filename.pass1.wav" automatically 
	 * @param srcFile source file
	 */
	public AudioConverter(String srcFile) {
		this.srcFile = srcFile;
		this.destFile = srcFile + ".pass1.wav";
	}
	
	public AudioConverter(String srcFile, String dstFile) {
		this.srcFile = srcFile;
		this.destFile = dstFile;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//System.out.println("convert start.");

		String command = RuntimeCFG.EXTERNAL_PROGRAM_DIR + "/sox \"" + 
			srcFile + "\" -b 16 -c 1 -r 44100 \"" + destFile + "\"";
		C.System(command);
		
	}
	
	public static void main(String args[]) {
		if (args.length != 2) {
			System.out.println("Usage: AudioConverter <source> <dest>");
		} else {
			AudioConverter ac = new AudioConverter(args[0], args[1]);
			ac.run();
		}
	}

	private String srcFile, destFile;
}
