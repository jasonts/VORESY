package voresy;
import flanagan.math.FourierTransform;

public class SpectrogramMTcore implements Runnable{

	public SpectrogramMTcore(double[] s, int i, String filename) {
		originalFilename = filename;
		audioclip = s;
		index = i;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		ft = new FourierTransform(audioclip);
		ft.setHann();
	
		System.out.println("STFT " + index + " start.");
		ft.shortTime(1024);
		System.out.println("STFT " + index + " finished.");
		
		String thisFilename = originalFilename + ".pass3_STFT." + index;
		ft.printShortTime(thisFilename + ".txt");
		
		//�N���ǿ�X���ɮ׵e��spectrogram
		new DrawSpectrogramFromFile(thisFilename);
	}
	
	private FourierTransform ft;
	private String originalFilename;
	private int index;
	private double[] audioclip;

}
