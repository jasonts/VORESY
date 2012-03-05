package voresy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import c_like.C;

/*
 * AudioNoiseRemover
 * 
 * Changelog:
 * 20110613_1: add a new construction
 */

public class AudioNoiseRemover {

	/**
	 * AudioNoiseRemover Constructor<br>
	 * This constructor is used for VORESY system.<br>
	 * the input file has set to "filename.pass1.wav" automatically, and the
	 * output file has set to "filename.pass2.wav" automatically
	 * 
	 * @param audio
	 *            AudioFile
	 * @param OutputNoiseSampleFile
	 *            Noise sample file name.
	 */
	public AudioNoiseRemover(String audio) {
		this.audio = new AudioFile(new File(audio + ".pass1.wav"));
		
		OriginalWAVFile = audio + ".pass1.wav";
		NoiseWAVFile = audio + ".pass2noise.wav";
		CleanedWAVFile = audio + ".pass2.wav";
		// SoX noise-profile
		SoX_Noise_Profile = audio + ".noiseprofile";
	}

	// 20110613_1: new construction
	// audio_tail: filename 是否自動加尾巴
	public AudioNoiseRemover(String audio, boolean audio_tail) {
		if (audio_tail)
			this.audio = new AudioFile(new File(audio + ".pass1.wav"));
		else
			this.audio = new AudioFile(new File(audio));

		this.audio.init();
		NoiseWAVFile = audio + ".pass2noise.wav";
		CleanedWAVFile = audio + ".pass2.wav";
		OriginalWAVFile = audio;
		// SoX noise-profile
		SoX_Noise_Profile = audio + ".noiseprofile";
	}

	public void start() {
		// TODO Auto-generated method stub
		
		audio.init();
		if (verbose) {
			System.out.println("De-noise filter start.");
			System.out.println("Duration: " + audio.getDurationSeconds()
					+ " secs (" + audio.getFrameLength() + " frames)");
		}
		List<Double> audioSamples_noise_AL = new ArrayList<Double>(100000);

		// ��n�����n���j�p����A���U�ӴN�O�j�M����ɮ״M����T
		// scan per 10ms
		for (int i = 0; i < audio.getFrameLength(); i += RuntimeCFG.ms*10) {
			double thisAmplitude = 
				audio.getDuringSamplesMeanAmplitude(i, (int) (RuntimeCFG.ms*10));
			
			if (thisAmplitude < audio.getAvgAmplitude()) {
				for (int j = i; j < i + RuntimeCFG.ms*10; j++) {
					try {	//out of range exception
						audioSamples_noise_AL.add(new Double(audio.getSample(j)));
					} catch (Exception e) {}
				}
			}
		}

		// ���쪺�Ҧ�double����T�T���নraw byte
		byte[] audioSamples_noise_byte = new byte[audioSamples_noise_AL.size() * 2];
		int bytePos = 0;
		
		for (int i = 0; i < audioSamples_noise_AL.size(); i++) {
			int audioSamples_noise_raw = (int) (audioSamples_noise_AL.get(i) * Math
					.pow(2., audio.getSampleSize() - 1));
			audioSamples_noise_byte[bytePos++] = (byte) (audioSamples_noise_raw);
			audioSamples_noise_byte[bytePos++] = (byte) (audioSamples_noise_raw >>> 8);
		}
		if (verbose)
			System.out.println("Scan Successfully.");
		byte WAVE_header[] = audio.WAVEHeaderGen(audioSamples_noise_AL.size());
		// ��X��T
		// �����Xheader�A�A��little-endian���覡��Xdata
		try {
			FileOutputStream fos = new FileOutputStream(NoiseWAVFile);
			fos.write(WAVE_header);
			fos.write(audioSamples_noise_byte, 0, audioSamples_noise_AL.size());
			fos.flush();
			fos.close();
			if (verbose)
				System.out.println("Successfully output noise audio.");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// SoX noise 2 pass removal
		String command;

		// pass 1: let SoX learn the noise feature
		System.out.println("SoX noise removal pass 1 start");
		command = RuntimeCFG.EXTERNAL_PROGRAM_DIR + "/sox " + NoiseWAVFile
				+ " -n noiseprof " + SoX_Noise_Profile;
		C.System(command);

		// pass 2: denoise from the noise profile
		System.out.println("SoX noise removal pass 2 start");
		command = RuntimeCFG.EXTERNAL_PROGRAM_DIR + "/sox " + OriginalWAVFile
				+ " " + CleanedWAVFile + " noisered " + SoX_Noise_Profile
				+ " " + strength;
		int result = C.System(command);
		if (verbose)
			System.out.println("pass 2: " + result);

		// ----------------------
		if (verbose)
			System.out.println("De-noise filter finished.");
		/*
		File delFile1 = new File(NoiseWAVFile);
		delFile1.delete();
		File delFile2 = new File(SoX_Noise_Profile);
		delFile2.delete();
*/
	}

	public void setStrength(double in) {
		strength = in;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

	private AudioFile audio;
	private String OriginalWAVFile;
	private String NoiseWAVFile;
	private String CleanedWAVFile;
	private String SoX_Noise_Profile;
	private boolean verbose = false;
	private double strength = 0.3;
}
