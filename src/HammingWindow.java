import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import voresy.AudioFile;


public class HammingWindow {

	public HammingWindow() {
		
		String filename = "/home/jason/文件/多多及林務局的nightmare/20110913_ade01/ade01_segment.wav";
		file = new AudioFile(new File(filename));
		file.init();
		
		int blocksize = 512;
		int blockOffset = blocksize /3;
		Vector<double[]> blocks = new Vector<double[]>();
		
		// put the "blocks" to container 
		int offset = 0;
		while (true) {
			if (offset >= file.getFrameLength()) 
				break;
			
			blocks.add(file.getSamples(offset, blocksize));
			offset = offset + blocksize - blockOffset;
		}
		
		Vector<double[]> hammedblocks = new Vector<double[]>();
		double[] hammingcurve = null;
		// hamming ( h(i) = 0.53836 - 0.46164*cos(2pi*i/isize) )
		for (int i = 0; i < blocks.size(); i++) {
			// calc this block size's hamming table. (W(n))
			double[] hammingTable = new double[blocks.get(i).length];
			for (int j = 0; j < hammingTable.length; j++) {
				hammingTable[j] = 0.53836 - 0.46164 
						* Math.cos((Math.PI * 2 * j) / hammingTable.length);
			}
			if (i == 0)
				hammingcurve = hammingTable;
			
			//calc hammed value
			double[] originalValue = blocks.get(i);
			double[] hammedValue = new double[originalValue.length];
			for (int j = 0; j < originalValue.length; j++) {
				hammedValue[j] = originalValue[j] * hammingTable[j];
			}
			hammedblocks.add(hammedValue);
			
		}
		
		//output per block csv
		for (int i = 0; i < blocks.size(); i++) {
			FileWriter fw, hfw;
			try {
				fw = new FileWriter(new File(filename + "." + i + ".csv"));
				hfw = new FileWriter(new File(filename +"." + i + ".hamming.csv"));
			
				//block section
				for (int j = 0; j < blocks.get(i).length; j++) {
					fw.write(Double.toString(blocks.get(i)[j]) + "\r\n");
				}
				fw.write("\r\n");
				fw.flush();
				
				//hamming section
				for (int j = 0; j < hammedblocks.get(i).length; j++) {
					hfw.write(Double.toString(hammedblocks.get(i)[j]) + "\r\n");
				}
				hfw.write("\r\n");
				hfw.flush();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		FileWriter hcfw;
		try {
			hcfw = new FileWriter(new File(filename + ".hammingcurve.csv"));
			for (int j = 0; j < hammingcurve.length; j++)
				hcfw.write(Double.toString(hammingcurve[j]) + "\r\n");
			hcfw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//hamming curve

		
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new HammingWindow();

	}
	
	private AudioFile file;

}
