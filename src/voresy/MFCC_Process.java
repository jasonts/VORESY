package voresy;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import mfcc.MFCC;

public class MFCC_Process {

	/**
	 * 
	 * Calc the MFCC paremeter with a AudioFile that has many specific feature clips.
	 * @param audio AudioFile
	 * @param sampleSignals clips, structure in signalclip's vector
	 */
	public MFCC_Process(AudioFile audio, Vector<signalclip> sampleSignals) {
		// TODO Auto-generated constructor stub
		this.audio = audio;
		this.sampleSignals = sampleSignals;
	}
	
	public MFCC_Process(String audio, Vector<signalclip> sampleSignals) {
		// TODO Auto-generated constructor stub
		this.audio = new AudioFile(new File(audio));
		this.audio.init();
		this.sampleSignals = sampleSignals;
	}
	
	/**
	 * 
	 * Calc the MFCC parameters in a single file with only a specific feature.
	 * @param audio AudioFile
	 */
	public MFCC_Process(AudioFile audio) {
		this.audio = audio;
		sampleSignals = new Vector<signalclip>();
		sampleSignals.add(new signalclip(0, (int)audio.getFrameLength()-1));
	}
	
	public void startMFCC() {
		paramMFCCht = new Hashtable<Integer, double[]>();
		
		for (int i = 0; i < sampleSignals.size(); i++) {
			
			//FFT length must be power of 2
			int expectedFFTlen = 0;
			for (int j = 0; j < 32; j++) {
				if (1 << j >= sampleSignals.get(i).getFrameLength()) {
					expectedFFTlen = 1 << j;
					break;
				}
			}
			
			//calc the x-axis MFCC
			/*
			 * public MFCC(int nnumberOfParameters,
				double dsamplingFrequency,
				int nnumberofFilters,
				int nFFTLength,
				boolean oisLifteringEnabled,
				int nlifteringCoefficient,
				boolean oisZeroThCepstralCoefficientCalculated)
			 */
			MFCC mfcc = new MFCC(RuntimeCFG.mfcc_axis, 44100, 24, expectedFFTlen, true, 22, false);
			
			double[] dparam = mfcc.getParameters(
					audio.getSamples(
							sampleSignals.get(i).getStartPosition(), sampleSignals.get(i).getFrameLength()));
			
			paramMFCCht.put(new Integer(i), dparam);
		}
		
	}
	
	public Hashtable<Integer, double[]> getClipsParameters() {
		return paramMFCCht;
	}
	
	public void outputMFCCarff(String filename) {
		try {
			FileWriter fw = new FileWriter(new File(filename));
			FileReader fr = null;
			fr = new FileReader(RuntimeCFG.WORKDIR + "/template.txt");
			
			//寫入ARFF的header
			while (true) {
				int i = fr.read();
				if (i == -1) 
					break;
				else
					fw.write(i);
			}
			
			//寫入收集到的MFCC
			for (int i = 0; i < paramMFCCht.size(); i++) {
				try {
					for (int j = 0; j < RuntimeCFG.mfcc_axis; j++) 
						fw.write(Double.toString(paramMFCCht.get(i)[j]) + ",");
					fw.write("台北樹蛙\r\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean standAlone = false;
	private AudioFile audio;
	private Hashtable<Integer, double[]> paramMFCCht;
	private Vector<signalclip> sampleSignals;

}
