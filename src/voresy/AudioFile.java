package voresy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;


public class AudioFile {
	
	public AudioFile(File file) {
		
		try {
			audioFile = AudioSystem.getAudioInputStream(file);
			audioFileName = file.getPath();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		audioFormat = audioFile.getFormat();
		audioFrameLength = audioFile.getFrameLength();
		audioSampleSize = audioFormat.getSampleSizeInBits();	//in bits
	}
	
	/**
	 * Import double signals to AudioFile (44.1k)
	 * @param signals signals
	 * @param SampleSize signals' sample size (8 or 16)
	 */
	public AudioFile(double[] signals, int SampleSize) {
		audioSamples = signals;
		audioSampleSize = SampleSize;
		audioFrameLength = audioSamples.length;
	}

	public AudioInputStream getAudioInputStream() {	return audioFile; }
	
	public AudioFormat getAudioFormat() { return audioFormat; }
	
	public int getSampleSize() { return audioSampleSize; }
	
	public long getFrameLength() { return audioFrameLength; }
	
	public double getFrameRate() { return audioFormat.getFrameRate(); }
	
	public double getDurationSeconds() {return audioFrameLength / audioFormat.getFrameRate();}
	
	public double getSample(int offset) { return audioSamples[offset]; }
	
	public double[] getSamples() { return audioSamples; }
	
	/**
	 * Get the specific range of samples
	 * @param offset initial position
	 * @param len length
	 * @return samples
	 */
	public double[] getSamples(int offset, int len) {
		
		try {
			double[] sample = new double[len];
			System.arraycopy(audioSamples, offset, sample, 0, len);
			return sample;
		} catch (IndexOutOfBoundsException e) {
			int length = (int)audioSamples.length - offset;
			double[] sample = new double[length];
			System.arraycopy(audioSamples, offset, sample, 0, length);
			return sample;
		}

	}
	
	/**
	 * Get the specific range of samples, it had "zerofilled" the tail when get last sample.
	 * @param offset initial position
	 * @param len length
	 * @return zerofilled samples
	 */
	public double[] getTailedSamples(int offset, int len) {
		
		try {
			double[] sample = new double[len];
			System.arraycopy(audioSamples, offset, sample, 0, len);
			return sample;
		} catch (IndexOutOfBoundsException e) {
			int length = (int)audioFrameLength % len;
			double[] sample = new double[len];
			System.arraycopy(audioSamples, offset, sample, 0, length);
			return sample;
		}

	}
	
	/**
	 * 
	 * @param offset	head position
	 * @param len		length
	 * @return			samples average amplitude
	 */
	public double getDuringSamplesMeanAmplitude(int offset, int len) {
		double result = 0;
		//判斷len是否太大
		if (offset + len >= audioFrameLength)
			len = (int)audioFrameLength - offset;
		//加起來
		for (int i = 0; i < len; i++)
			result += Math.abs(audioSamples[offset+i]);
		//平均
		result /= len;
		
		return result;
	}
	/**
	 * Get the max amplitude of selected samples in "double" format.
	 * @param offset initial position
	 * @param len length
	 * @return max amplitude in double
	 */
	public double getDuringSamplesMaxAmplitude(int offset, int len) {
		double result = 0;
		if (offset + len >= audioFrameLength) 
			len = (int)audioFrameLength - offset;
		for (int i = 0; i < len; i++) {
			if (result < Math.abs(audioSamples[offset+i]))
				result = Math.abs(audioSamples[offset+i]);
		}
		return result;
		
	}

	public int getDuringSamplesMaxAmplitudePosition(int offset, int len) {
		int result = offset;
		if (offset + len >= audioFrameLength) 
			len = (int)audioFrameLength - offset;
		for (int i = result+1; i < offset+len ; i++) {
			if (Math.abs(audioSamples[result]) < Math.abs(audioSamples[i]))
				result = i;
		}
		return result;
		
	}

	public double getAvgAmplitude() {
		return audioAvgAmp;
	}
	
	public void init() {
		//將原始的audio數值讀進audioBytes (byte[])
		audioBytes = new byte[(int) (audioFrameLength * audioSampleSize)];
		try {
			audioFile.read(audioBytes);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//將audioBytes轉換為 audiosamples (byte[] -> double[])
		audioSamples = new double[(int) audioFile.getFrameLength()];
		//big-endian or little-endian
		int sampleSizeInBytes = audioSampleSize/8;
		int[] sampleBytes = new int[sampleSizeInBytes];
		int k = 0;	//index in audioBytes
		for (int i = 0; i < audioFrameLength; i++) {
			//collect sample byte in big-endian order
			if (audioFormat.isBigEndian()) {
				//bytes start with MSB
				for (int j = 0; j < sampleSizeInBytes; j++)
					sampleBytes[j] = audioBytes[k++];
			} else {
				//bytes start with LSB
	            for (int j = sampleSizeInBytes - 1; j >= 0; j--) {
	                sampleBytes[j] = audioBytes[k++];
	                if (sampleBytes[j] != 0)
	                    j = j + 0;
	            }
	        }
			//get integer value from bytes
			int ival = 0;
			for (int j = 0; j < sampleSizeInBytes; j++) {
				ival += sampleBytes[j];
				if (j < sampleSizeInBytes - 1) ival <<=8;
			}
			//decode value
			double ratio = Math.pow(2., audioSampleSize-1);
			double val = ((double)ival) / ratio;
			audioSamples[i] = val;
		}
		
		//計算聲音檔的平均響度
		audioAvgAmp = 0;
		double period = audioFormat.getFrameRate()/100;
		for (int i = 0; i < audioFrameLength; i = i + (int)period) 
			audioAvgAmp += Math.abs(audioSamples[i]/(audioFrameLength/period));
/*
		//more accurate average amplitude
		//以500 samples為單位，先算這單位的平均，然後再算這些的平均，就是整個聲音檔的平均響度
		double oldAudioAvgAmp = audioAvgAmp;
		audioAvgAmp = 0;
		int blocks_qty = ((int)audioFrameLength)/500 + 1;//blocks.length;
		for (int i = 0; i < blocks_qty; i++)
			audioAvgAmp += getDuringSamplesMeanAmplitude(i * 500, 500);
		audioAvgAmp /= blocks_qty;
		
		System.out.println("old: " + oldAudioAvgAmp + "\r\n" +
				"new: " + audioAvgAmp);
*/
	}
	
	//��e��440��samples�A�`�@881��samples(20ms)�Ӭ�ZCR
	public void startZCR() {
		int bound = 440;
		audioSamplesZCRperFrame = new boolean[audioSamples.length];
		
		//��ݨC�@��frame���U�@��sample�O���O�L0�A������n��
		for (int i = 0; i < audioSamplesZCRperFrame.length-1; i++) {
			//�����ҩ�Ajava�����k�B����Ѥ����P�_�@���@�t�٧�...
			if (audioSamples[i] * audioSamples[i+1] < 0)
				audioSamplesZCRperFrame[i] = true;
			else
				audioSamplesZCRperFrame[i] = false;
		}
		
		/*
		 * 1������sample�A300ms
		 */
		
		audioSamplesZCR = new int[audioSamples.length];
		
		//��Ͳ�0��
		for (int i = 0; i < bound; i++) 
			if (audioSamplesZCRperFrame[i]) 
				audioSamplesZCR[0]++;
		//��1��~��255�ӡA�ѦҤU�@���ܤƴN�n
		for (int i = 1; i < bound; i++) {
			audioSamplesZCR[i] = audioSamplesZCR[i-1];
			//upperbound
			if (audioSamplesZCRperFrame[i+bound]) 
				audioSamplesZCR[i]++;
		}
		//��255�Ө�̫�255�ӡA�W�U���n�Ѧ�
		for (int i = bound; i < audioSamplesZCR.length - bound; i++) {
			audioSamplesZCR[i] = audioSamplesZCR[i-1];
			//upperbound
			if (audioSamplesZCRperFrame[i+bound]) 
				audioSamplesZCR[i]++;
			//lowerbound
			if (audioSamplesZCRperFrame[i-bound]) 
				audioSamplesZCR[i]--;
		}
		//�̫�255�ӡA�ѦҤW�@���ܤƴN�n
		for (int i = audioSamplesZCR.length - bound; i < audioSamplesZCR.length; i++) {
			audioSamplesZCR[i] = audioSamplesZCR[i-1];
			//lowerbound
			if (audioSamplesZCRperFrame[i-bound]) 
				audioSamplesZCR[i]--;
		}
		
		//��ZCR���֭�
		int minZCR = bound*2;
		for (int i = 0; i < audioSamplesZCR.length; i++) {
			if (ZCRThreshold < audioSamplesZCR[i])
				ZCRThreshold = audioSamplesZCR[i];
			if (minZCR > audioSamplesZCR[i])
				minZCR = audioSamplesZCR[i];
		}
		ZCRThreshold *= 0.3;
		
		//���t�ΰ�gc
		audioSamplesZCRperFrame = null;
		
	}
	
	public void dumpAudioCSV() {
		
		try {
			BufferedWriter write = new BufferedWriter(
					new FileWriter(RuntimeCFG.WORKDIR + "/TEMP/ZCR.csv"));
			for (int i = 0; i < audioSamplesZCR.length; i++)
				write.write(i + "," + Math.abs(audioSamples[i]) + "," +
						audioSamplesZCR[i] + "," + ZCRThreshold + "\r\n");
			write.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param outputFilename filename
	 * @param s vector signalclip
	 */
	public void dumpAudioCSVWithSelection(String outputFilename, Vector<signalclip> s) {
		byte signal[] = new byte[audioSamples.length];

		for (int h = 0; h < s.size(); h++) {
			for (int j = s.get(h).getStartPosition(); j <= s.get(h).getEndPosition(); j++) 
				signal[j] = 1;	//"1"
		}

		try {
			BufferedWriter write = new BufferedWriter(
					new FileWriter(outputFilename));
			for (int i = 0; i < audioSamples.length; i++) {
				/*
				 * file structure: 
				 * sample index, amplitude, selection, (ZCR rate, ZCR threshold)
				 */
				StringBuffer strBuf = new StringBuffer(
						new String(i + "," + 
									Math.abs(audioSamples[i]) + "," +
									signal[i] + ","));
				if (!RuntimeCFG.NORS)
					strBuf.append(
							new String(audioSamplesZCR[i] + "," +
										ZCRThreshold));
				strBuf.append("\r\n");
				write.write(strBuf.toString());
			}
			write.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param outputFilename filename
	 * @param s vector signalclip
	 */
	public void dumpAudioCSVWithSelection(String outputFilename, Vector<signalclip> s,
			double noiseThrd) {
		byte signal[] = new byte[audioSamples.length];

		for (int h = 0; h < s.size(); h++) {
			for (int j = s.get(h).getStartPosition(); j <= s.get(h).getEndPosition(); j++) 
				signal[j] = 1;	//"1"
		}

		try {
			BufferedWriter write = new BufferedWriter(
					new FileWriter(outputFilename));
			for (int i = 0; i < audioSamples.length; i++) {
				/*
				 * file structure: 
				 * sample index, amplitude, selection, energy thrd, (ZCR rate, ZCR threshold)
				 */
				StringBuffer strBuf = new StringBuffer(
						new String(i + "," + 
									Math.abs(audioSamples[i]) + "," +
									signal[i] + "," + noiseThrd + ","));
				if (!RuntimeCFG.NORS)
					strBuf.append(
							new String(audioSamplesZCR[i] + "," +
										ZCRThreshold));
				strBuf.append("\r\n");
				write.write(strBuf.toString());
			}
			write.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int getZCRThreshold() {
		return ZCRThreshold;
	}
	
	public int getSampleZCR(int offset) {
		return audioSamplesZCR[offset];
	}
	
	/**
	 * 
	 * @param offset	the position
	 * @param len		length
	 * @param LR		true:right, false:left
	 * @return			ZCR samples array
	 */
	public int[] getDuringSamplesZCR(int offset, int len, boolean LR) {
		int result[];
		if (LR) {
			//往左
			if (offset+len >= audioSamplesZCR.length) 
				len = audioSamplesZCR.length - offset;
			result = new int[len];
			System.arraycopy(audioSamplesZCR, offset, result, 0, len);
		} else {
			//往右
			if (offset - len < 0) 
				len = offset;
			result = new int[len];
			System.arraycopy(audioSamplesZCR, offset-len, result, 0, len);
		}
		return result;
	}
	
	/**
	 * Output the audio clips from signalclip
	 * @param clips signalclip vector
	 * @param filename the filename, "*" would put the clips index
	 */
	public void saveAudioClips(Vector<signalclip> clips, String filename) {
		// TODO Auto-generated method stub
		
		String name[] = filename.split(">InDeX<");
		System.out.println("Starting output the Audio Clips ...");
		for (int i = 0; i < clips.size(); i++) {
			int len = clips.get(i).getEndPosition() - clips.get(i).getStartPosition();
			String outFilename = name[0] + String.format("%02d", i) + name[1];
			saveAudioClip(clips.get(i).getStartPosition(), len, outFilename);
		}
		System.out.println("Ouptut the Audio Clips successfully.");
	}

	private void saveAudioClip(int offset, int len, String filename) {
		// TODO Auto-generated method stub
		byte WAVE_header[] = WAVEHeaderGen(len);
		try {
			FileOutputStream fos = 
				new FileOutputStream(filename);
			fos.write(WAVE_header);
			
			byte signal[] = new byte[2];
			for (int i = offset; i < offset + len; i++) {
				signal[0] = audioBytes[i*2];
				signal[1] = audioBytes[i*2+1];
				fos.write(signal);
			}
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveAudioClip(signalclip clip, String filename) {
		// TODO Auto-generated method stub
		int start = clip.getStartPosition();
		int len = clip.getEndPosition() - start;
		saveAudioClip(start, len, filename);
	}

	/**
	 * WAVE Header Generator.<br>
	 * Reference: https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
	 * 
	 * @param NumSamples	Amounts of the frames.
	 * @return				WAVE Header information.
	 */
	public byte[] WAVEHeaderGen(int NumSamples) {
		byte WAVE_header[] = new byte[44];
		
		//RIFF header
		//ChunkID: "RIFF", 4 bytes
		WAVE_header[0] = 0x52;
		WAVE_header[1] = 0x49;
		WAVE_header[2] = 0x46;
		WAVE_header[3] = 0x46;
		//Format: "WAVE", 4 bytes
		WAVE_header[8] = 0x57;
		WAVE_header[9] = 0x41;
		WAVE_header[10] = 0x56;
		WAVE_header[11] = 0x45;
		
		//fmt header
		//Subchunk1ID: "fmt ", 4bytes
		WAVE_header[12] = 0x66;
		WAVE_header[13] = 0x6d;
		WAVE_header[14] = 0x74;
		WAVE_header[15] = 0x20;
		//Subchunk1Size: 16 for PCM, 0x10000000, 4bytes
		WAVE_header[16] = 0x10;
		WAVE_header[17] = 0x00;
		WAVE_header[18] = 0x00;
		WAVE_header[19] = 0x00;
		//AudioFormat: PCM = 1, 2bytes
		WAVE_header[20] = 0x01;
		WAVE_header[21] = 0x00;
		//NumChannels: Mono = 1, 2bytes
		WAVE_header[22] = 0x01;
		WAVE_header[23] = 0x00;
		//SampleRate: 44100, 0x44ac0000, 4bytes
		WAVE_header[24] = 0x44;
		WAVE_header[25] = (byte) 0xAC;
		WAVE_header[26] = 0x00;
		WAVE_header[27] = 0x00;
		//ByteRate: 44100 * 1 * (16/8) = 88200, 0x88580100, 4bytes
		WAVE_header[28] = (byte) 0x88;
		WAVE_header[29] = 0x58;
		WAVE_header[30] = 0x01;
		WAVE_header[31] = 0x00;
		//BlockAlign: 1 * 16 / 8 = 2, 0x0200, 2bytes
		WAVE_header[32] = 0x02;
		WAVE_header[33] = 0x00;
		//BitsPerSample: 16 bits, 0x1000, 2bytes
		WAVE_header[34] = 0x10;
		WAVE_header[35] = 0x00;
		
		//data header
		//Subchunk2ID: "data", 4bytes
		WAVE_header[36] = 0x64;
		WAVE_header[37] = 0x61;
		WAVE_header[38] = 0x74;
		WAVE_header[39] = 0x61;
		//subchunk2Size: NumSamples * 1 * (16/8)
		int subchunk2Size = NumSamples * 2;
		byte[] subchunk2Size_ba = intToByteArray(subchunk2Size);
		WAVE_header[40] = subchunk2Size_ba[3];
   		WAVE_header[41] = subchunk2Size_ba[2];
   		WAVE_header[42] = subchunk2Size_ba[1];
   		WAVE_header[43] = subchunk2Size_ba[0];
   		
   		//chunkSize: 4 + (8 + 16) + (8 + SubChunk2Size), 4bytes
   		int chunkSize = 36 + subchunk2Size;
   		byte[] chunkSize_ba = intToByteArray(chunkSize);
   		WAVE_header[4] = chunkSize_ba[3];
   		WAVE_header[5] = chunkSize_ba[2];
   		WAVE_header[6] = chunkSize_ba[1];
   		WAVE_header[7] = chunkSize_ba[0];
   		
   		return WAVE_header;
	}
	
	public static final byte[] intToByteArray(int value) {
		return new byte[] {
				(byte)(value >>> 24), (byte)(value >>> 16), (byte)(value >>> 8), (byte)value };
	}
	
	public String getAudioFilename() {
		return audioFileName;
	}
	
	private AudioInputStream audioFile;
	private String audioFileName;
	private AudioFormat audioFormat;
	private byte[] audioBytes;
	private long audioFrameLength;
	private int audioSampleSize;
	private double[] audioSamples;
	private int[] audioSamplesZCR;
	private int ZCRThreshold = 0;
	private boolean[] audioSamplesZCRperFrame;
	private double audioAvgAmp;
	
}
