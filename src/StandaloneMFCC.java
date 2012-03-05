
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;

import voresy.AudioFile;
import voresy.MFCC_Process;
import voresy.RuntimeCFG;

public class StandaloneMFCC {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ht = new Hashtable<Integer, double[]>(200);
		String file = "/home/jason/文件/林務局的nightmare/20111128/SAMPLES/denoised/RS_過頭/";
		String fileList[] = new File(file).list();
		Arrays.sort(fileList);
		//9,12,15,18
		//RuntimeCFG.mfcc_axis = 18;
		for (int i = 0; i < fileList.length; i++) {
			if (!fileList[i].endsWith("wav")) continue;
			System.out.println("processing: " + i + ", " + fileList[i]);
			
			audio = new AudioFile(new File(file + fileList[i]));
			audio.init();
			mp = new MFCC_Process(audio);
			mp.standAlone = true;
			mp.startMFCC();
			ht.put(i, mp.getClipsParameters().get(0));
			
		}
		System.out.println();
		
		
		//output to MFCC.csv

		try {
			FileWriter fw = new FileWriter(new File(file + "MFCC" + RuntimeCFG.mfcc_axis + ".csv"));
			FileWriter fw2 = new FileWriter(new File(file + "MFCC" + RuntimeCFG.mfcc_axis + ".arff"));
			FileReader fr = null;
			fr = new FileReader("/home/jason/文件/林務局的nightmare/20111128/template.txt");
			
			//寫入ARFF的header
			/*
			fw2.write("@RELATION 夜行性\r\n\r\n");
			for (int i = 0; i < RuntimeCFG.mfcc_axis; i++) {
				fw2.write("@ATTRIBUTE MFCC" + i + " REAL\r\n");
			}
			*/
			while (true) {
				int i = fr.read();
				if (i == -1) 
					break;
				else
					fw2.write(i);
			}
			
			//寫入收集到的MFCC
			for (int i = 0; i < fileList.length; i++) {
				try {
					for (int j = 0; j < RuntimeCFG.mfcc_axis; j++) {
						fw.write(Double.toString(ht.get(i)[j]) + ",");
						fw2.write(Double.toString(ht.get(i)[j]) + ",");
					}
					String split[] = fileList[i].split("_");
					fw.write(fileList[i] + "\r\n");
					fw2.write(split[0] + "\r\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
			fw.flush();
			fw2.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Hashtable<Integer, double[]> ht;
	private static AudioFile audio;
	private static MFCC_Process mp;

}
