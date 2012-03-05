
import java.io.File;
import c_like.C;

import voresy.AudioNoiseRemover;

public class StandaloneANR {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String file = "/home/jason/文件/多多及林務局的nightmare/20111128/SAMPLES/";
		String fileList[] = new File(file).list();
		//Thread thread[] = new Thread[RuntimeCFG.THREADS];
		Thread thread[] = new Thread[1];
		
		for (int i = 0; i < fileList.length; i++) {
			if (!fileList[i].endsWith("wav")) 
				continue;
			int idle_thread = -1;
			while (true) {
				for (int j = 0; j < thread.length; j++) {
					if (thread[j] == null || thread[j].isAlive() == false) { 
						idle_thread = j;
						break;
					}
				}
				if (idle_thread == -1)
					C.sleep(55);
				else
					break;
			}
			
			System.out.println("processing: #" + i + " " + (file + fileList[i]));
			thread[idle_thread] = new Thread(new ANR(file + fileList[i]));
			thread[idle_thread].start();
			
		}
		System.out.println();

	}

}

class ANR implements Runnable {
	public ANR(String file) {
		anr = new AudioNoiseRemover(file, false);
		//anr.setStrength(0.5);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		anr.start();
		
	}
	private AudioNoiseRemover anr;
}