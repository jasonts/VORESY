
import java.io.File;
import voresy.RS_EPD_MT;
import voresy.RuntimeCFG;

public class StandaloneRS {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String file = "/home/jason/文件/林務局的nightmare/20111128/SAMPLES/denoised/過頭抓不到/";
		String fileList[] = new File(file).list();
		RuntimeCFG.THREADS = 2;
		Thread threads[] = new Thread[RuntimeCFG.THREADS];
		
		//RuntimeCFG.RS_CSVLOG_3 = true;
		//RuntimeCFG.RS_EXPORTWAV = true;
		//RuntimeCFG.RS_CSVLOG_1 = true;
		//RuntimeCFG.RS_CSVLOG_2 = true;
		//70ms, comment: 200ms
		//RuntimeCFG.minimalSegmentLen = 70;

		for (int i = 0; i < fileList.length; i++) {
			int idle_thread = -1;
			if (fileList[i].endsWith("wav") == false) continue;
			
			while(true) {
				for (int j = 0; j < RuntimeCFG.THREADS; j++) {
					if (threads[j] == null || (threads[j].isAlive() == false)) {
						idle_thread = j;
						break;
					}
				}
				if (idle_thread == -1) {
					try {
						Thread.sleep(55);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else
					break;
			}

			if (fileList[i].lastIndexOf("wav") != -1) {
				System.out.println("processing: " + i + ", "+ fileList[i]);
				threads[idle_thread] = new Thread(new RS_EPD_MT(file + fileList[i]));
				threads[idle_thread].start();
				System.out.println("");
			}
		}
		
		
		while (true) {
			boolean finish = true;
			for (int i = 0; i < RuntimeCFG.THREADS; i++) {
				//any thread were busy
				if (!(threads[i] == null || threads[i].isAlive() == false)) {
					finish = false;
					break;
				}
			}
			if (finish) break;
		}

		System.out.println("finished!!!");
	}

}
