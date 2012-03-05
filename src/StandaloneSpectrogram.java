import java.io.File;

import voresy.Spectrogram;


public class StandaloneSpectrogram {
	
	public StandaloneSpectrogram() {		
		String file = "/home/jason/文件/多多及林務局的nightmare/20111005_結案_書面物種輸出/";
		String fileList[] = new File(file).list();
		for (int i = 0; i < fileList.length; i++) {
			if (!fileList[i].endsWith("wav")) 
				continue;
			Spectrogram s = new Spectrogram(file + fileList[i]);
			s.start();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new StandaloneSpectrogram();

	}

}
