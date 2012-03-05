package voresy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class converter_core {

	public converter_core(String src, String dstDIR) {
		srcfilename = src.toString();
		setdestFileName(dstDIR.toString());
	}

	public void setdestFileName(String dir) {
		convertedFile = dir + "/pass1.wav";
		NoiseWAVFile = dir + "/noise.wav";
		CleanedWAVFile = dir + "/cleaned.wav";
		srcfiletype = getExtension(srcfilename);
		printDestFileNameList();
	}
	
	protected void printDestFileNameList() {
		System.out.println("Source File: " + srcfilename);
		System.out.println("Source Filetype: " + srcfiletype);
		System.out.println("Converted File: " + convertedFile);
		System.out.println("Noise File: " + NoiseWAVFile);
		System.out.println("Cleaned File: " + CleanedWAVFile);
	}
	
	public void start() {
		
		//--------------------------
		//Pass 1: convert any to 44k16b mono wav
		//        using SoX
		//--------------------------
		fmtconvert();
	
		//---------------------------
		//pass 1.9: ��N���ɫ᪺�ɮ׷ǳƦn�A�o�����ӥi�H�Y�u�@�I�ɶ�
		//
		//---------------------------
		audio = new AudioFile(new File(convertedFile));
		audio.init();
		
		//-----------------------------
		//pass 2: de-noise by using SoX
		//
		//method: 1. determine the noise position
		//        2. de-noise using the sample
		//-----------------------------
		denoise();
		
		//-----------------------------
		//pass 3: draw the spectrogram
		//
		//appendix: ���CCPU&RAM�ϥζq�����n�ؼ�
		//-----------------------------
		//spectrogram();

	}
	
	private void fmtconvert() {
		// TODO Auto-generated method stub
		AudioConverter ac = new AudioConverter(srcfilename, convertedFile);
		ac.run();
/*		
		System.out.println("convert start.");
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = null;
			String command = SoX_Path + "/sox.exe \"" + srcfilename + "\" -b 16 -c 1 -r 44100 \"" + convertedFile + "\"";
			//start the SoX
			proc = rt.exec(command);
            InputStream stderr = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ( (line = br.readLine()) != null)
                System.out.println(line);
            System.out.println("convert successfully.");
            int exitVal = proc.waitFor();
            System.out.println(exitVal);
            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/		
	}

	private void denoise() {
		System.out.println("De-noise filter start.");
		//duration: ���X��
		double duration = audio.getDurationSeconds();
		long duration_frame = audio.getFrameLength();
		int sampleSize_bit = audio.getSampleSize();
		System.out.println("Duration: " + duration + " secs (" + duration_frame + " frames)");

		List<Double> audioSamples_noise_AL = new ArrayList<Double>();
		
		double average_linear = audio.getAvgAmplitude();

		//��n�����n���j�p����A���U�ӴN�O�j�M����ɮ״M����T
		for (int i = 0; i < duration_frame; i++) {
			double thisSample = audio.getSample(i);
			
			//�p�G�o�{�o��frame�񥭧��٤p�n�A�N���frame�[����TArrayList
			if (thisSample < average_linear) 
				audioSamples_noise_AL.add(thisSample);
			
			//verbose
			if (i % 1000 == 0 && RuntimeCFG.DEBUG) 
				System.out.println("scanned " + i + " frames.");
		}

		//���쪺�Ҧ�double����T�T���নraw byte
		byte[] audioSamples_noise_byte = new byte[audioSamples_noise_AL.size()*2];
		int bytePos = 0;
		for (int i = 0; i < audioSamples_noise_AL.size(); i++) {
			int audioSamples_noise_raw = (int)(audioSamples_noise_AL.get(i) * Math.pow(2., sampleSize_bit-1));
			audioSamples_noise_byte[bytePos++] = (byte)(audioSamples_noise_raw);
			audioSamples_noise_byte[bytePos++] = (byte)(audioSamples_noise_raw>>>8);
		}
		System.out.println("Scan Successfully.");
		byte WAVE_header[] = wavheader_gen(audioSamples_noise_AL.size());
		//��X��T
		//�����Xheader�A�A��little-endian���覡��Xdata
		try {
			FileOutputStream fos = new FileOutputStream(NoiseWAVFile);
			fos.write(WAVE_header);
			fos.write(audioSamples_noise_byte, 0, audioSamples_noise_AL.size());
			fos.flush();
			fos.close();
			System.out.println("Successfully output noise audio.");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//SoX noise 2 pass removal
		double strength = 0.4;	//�h��T���j��
		try {
			System.out.println("SoX noise removal pass 1 start");
			// pass 1
			String command = SoX_Path + "/sox.exe " + NoiseWAVFile + " -n noiseprof noise-profile";
			Process proc = Runtime.getRuntime().exec(command);

			//debug purpose start
            InputStream stderr = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ( (line = br.readLine()) != null)
                System.out.println(line);
            int exitVal = proc.waitFor();
            System.out.println(exitVal);
            //debug purpose end
            
    		System.out.println("SoX noise removal pass 2 start");
            // pass 2
			String command2 = SoX_Path + "/sox.exe " + convertedFile + " " + CleanedWAVFile + " noisered noise-profile " + strength;			
			Process proc2 = Runtime.getRuntime().exec(command2);
			
			//debug purpose start
            InputStream stderr2 = proc2.getErrorStream();
            InputStreamReader isr2 = new InputStreamReader(stderr2);
            BufferedReader br2 = new BufferedReader(isr2);
            String line2 = null;
            while ( (line2 = br2.readLine()) != null)
                System.out.println(line2);
            int exitVal2 = proc.waitFor();
            System.out.println(exitVal2);
            
		} catch (Exception e) {	e.printStackTrace(); }
		
		//----------------------
		System.out.println("De-noise filter finished.");
	}
	
	//����wav header���禡
	// https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
	private byte[] wavheader_gen(int NumSamples) {
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
/*
	//Multi-Threaded
	private void spectrogram() {

		// ��l�� threads
		// stepping: ���q��
		Thread threads[] = new Thread[RuntimeCFG.THREADS];
		int stepping = 44100;
		for (int i = 0; i < audio.getFrameLength(); i = i + stepping) {

			int idle_Thread = -1;
			while (true) {
				//�ˬd���S�����m��thread
				//�p�G���A�N���Xwhile�ë�s��q�A�_�h�𮧤@�U�A�ˬd���S�����m��thread
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
			//���F�`�ٰO����A�w�g���槹��thread�n���gc
			//�j���|�W�[150ms*sample��ƪ��ɶ�
			//��̫᪺�ܧ쪺�����O"����1�?�a���0"��sample
			threads[idle_Thread] = new Thread(
					new SpectrogramMTcore(audio.getTailedSamples(i, stepping), i/stepping, CleanedWAVFile));
			threads[idle_Thread].start();
			System.gc();
			
		}
	}
	*/
	private String getExtension(String filename) {
		return filename.substring(filename.lastIndexOf(".")+1);
	}
	
	private String srcfilename;
	private String srcfiletype;
	private String convertedFile;
	private String SoX_Path = "/VORESY/INCLUDE";
	private String NoiseWAVFile;
	private String CleanedWAVFile;
	private AudioFile audio;	//�h��T�����W�Ъ��ӷ��ɮ�

}
