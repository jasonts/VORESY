package voresy;
import java.io.File;


public class Spectrogram {
	
	public Spectrogram(String audio) {
		this.audio = new AudioFile(new File(audio));
		originalFilename = audio;
	}
	
	public void start() {
		// TODO Auto-generated method stub
		audio.init();
		
		Thread threads[] = new Thread[RuntimeCFG.THREADS];
		int stepping = RuntimeCFG.sec;
		for (int i = 0; i < audio.getFrameLength(); i = i + stepping) {

			int idle_Thread = -1;
			while (true) {
				for (int j = 0; j < threads.length; j++) {
					if (threads[j] == null || (threads[j].isAlive() == false)) {
						idle_Thread = j;
						break;
					} 
				}
				if (idle_Thread == -1) {
					try {
						Thread.sleep(55);
					} catch (InterruptedException e) { e.printStackTrace(); }
				} else break;
			}

			threads[idle_Thread] = new Thread(
					new SpectrogramMTcore(audio.getTailedSamples(i, stepping), i/stepping, originalFilename));
			threads[idle_Thread].start();
			System.gc();
		}
		
	}
	
	private AudioFile audio;
	private String originalFilename;
}
