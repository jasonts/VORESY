package voresy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.io.FileUtils;

import c_like.C;

/*
 * Nocturnal VORESY: Nocturnal VOice REcognition SYstem
 * 
 * Version Revision
 * rev 1: 初始版
 * rev 2: 改變流程，先處理決策樹，之後再做進來資料的RS、MFCC、測試...
*/
public class VORESY {
	
	public VORESY(String srcFile, String dctreeFile) {
		
		//設立時間點來算每一步驟執行時間
		long initial_time = System.currentTimeMillis();
		
		/*
		 * Step 1: 建立決策樹
		 */
		DCtree dct = new DCtree(RuntimeCFG.WORKDIR + "/dctree.dc");		//決策樹的class位置
		String trainData = new String(srcFile + ".train.arff");			//產生出來的weka training data檔案名稱

		try {
			BufferedReader dcbr = new BufferedReader(new FileReader(dctreeFile));		//使用者的設定檔
			Integer pos = null;
			Integer liveIn = null;
			Double airTemp = null;
			Integer height = null;
			Integer month = null;
			while (true) {
				String in = dcbr.readLine();
				if (in == null) break;
				String spin[] = in.split("=");
				switch (dcParam.valueOf(spin[0]).ordinal()) {
				case 0:
					pos = new Integer(spin[1]);
					break;
				case 1:
					liveIn = new Integer(spin[1]);
					break;
				case 2:
					airTemp = new Double(spin[1]);
					break;
				case 3:
					height = new Integer(spin[1]);
					break;
				case 4:
					month = new Integer(spin[1]);
				default:
				}
			}
			
			//如果全都是空的話丟出exception，應該可以提升沒有決策樹的速度
			if (pos == null && liveIn == null &&
					airTemp == null && height == null && month == null)
				throw new NullPointerException();
			
			//建立符合及不符合條件的物種清單
			dct.filterFromParam(pos, liveIn, airTemp, height, month);
			ArrayList<String> included = dct.getIncludedSpecies();
			ArrayList<String> excluded = dct.getEncludedSpecies();
			
			FileUtils.copyFile(new File(RuntimeCFG.WORKDIR + "template.txt"), new File(trainData));
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(trainData, true));
			
			//將符合條件的物種輸出在前面
			for (int i = 0; i < included.size(); i++) {
				try {
					BufferedInputStream bis = new BufferedInputStream(
							new FileInputStream(RuntimeCFG.WORKDIR + "/MFCC/" + included.get(i) + ".txt"));
					byte[] in = new byte[1];
					
					while (bis.read(in) != -1) {
						bos.write(in);
					}
					bis.close();
				} catch (Exception f) {}//可能會有找不到物種training data exception
			}
			//其他物種輸出在後面
			for (int i = 0; i < excluded.size(); i++) {
				try {
				BufferedInputStream bis = new BufferedInputStream(
						new FileInputStream(RuntimeCFG.WORKDIR + "/MFCC/" + excluded.get(i) + ".txt"));
				byte in[] = new byte[1];
				while (bis.read(in) != -1) {
					bos.write(in);
				}
				bis.close();
				} catch (Exception ee) {}//可能會有找不到物種training data exception
			}
			
			bos.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			trainData = RuntimeCFG.trainData;
		}
		
		if (time_measure)
			System.out.println("Step 1: " + (System.currentTimeMillis()-initial_time)/1000);

		/*
		 * Step 2: 設定WEKA，然後training
		 */
		WEKAClassify wc = new WEKAClassify(trainData, srcFile + ".arff");
		wc.startTraining();

		if (time_measure)
			System.out.println("Step 2: " + (System.currentTimeMillis()-initial_time)/1000);

		/*
		 * Step 3: 檢查testing 的聲音檔是否已經完成，是則繼續往下做
		 */
		File f = new File(srcFile);
		while (f.exists() == false) {
			C.sleep(500);
		}
		
		/*
		 * Step 4: 轉檔，去雜訊
		 */
		// 轉檔(srcFile.pass1.wav)
		AudioConverter ac = new AudioConverter(srcFile);
		ac.run();
		ac = null;
		// 去雜訊(srcFile.pass2.wav)
		AudioNoiseRemover anr = new AudioNoiseRemover(srcFile);
		anr.start();
		anr = null;
		// 輸出光譜(srcFile.pass3.<count>.bmp)
		//Spectrogram spec = new Spectrogram(srcFile);
		//spec.start();
		//spec = null;

		if (time_measure)
			System.out.println("Step 4: " + (System.currentTimeMillis()-initial_time)/1000);

		/*
		 * Step 5: RS端點偵測
		 */
		RS_EPD rs = new RS_EPD(srcFile + ".pass2.wav");
		if (rs.getSignalClips().size() == 0) {
			System.out.println("0");
			return;
		}
		
		if (time_measure)
			System.out.println("Step 5: " + (System.currentTimeMillis()-initial_time)/1000);

		/*
		 * Step 6: MFCC
		 */
		MFCC_Process mp = new MFCC_Process(srcFile + ".pass2.wav", rs.getSignalClips());
		mp.startMFCC();
		mp.outputMFCCarff(srcFile + ".arff");
		
		if (time_measure)
			System.out.println("Step 6: " + (System.currentTimeMillis()-initial_time)/1000);

		/*
		 * Step 7: 開始測試並獲得結果
		 */
		wc.startTesting();
		ArrayList<Double> res = wc.getClassifyResult();

		if (time_measure)
			System.out.println("Step 7: " + (System.currentTimeMillis()-initial_time)/1000);

		/*
		 * Step 8: 計算片段的%數量
		 */
		ArrayList<species_mapping> map = new ArrayList<species_mapping>();
		try {
			BufferedReader br2 = new BufferedReader(
					new FileReader(RuntimeCFG.WORKDIR + "/mapping.txt"));
			int i = 0;
			while (true) {
				String re = br2.readLine();
				if (re == null) break;
				String in[] = re.split(" ");
				map.add(new species_mapping(i++, in[0], in[1]));
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//species_count: 第i個物種有n個片段
		int species_count[] = new int[map.size()];
		for (int i = 0; i < res.size(); i++) {
			species_count[res.get(i).intValue()]++;
		}

		//把辨識出來的物種類別和機率儲存到string & double ArrayList
		ArrayList<String> species_code = new ArrayList<String>();
		ArrayList<Double> species_prob = new ArrayList<Double>();
		for (int i = 0; i < map.size(); i++) {
			if (species_count[i] != 0) {
				species_code.add(map.get(i).code);
				species_prob.add(new Double(
						(double)species_count[i]/res.size()));
			}
		}
		//bubble sort is enough
		for (int i = 0; i < species_code.size()-1; i++) {
			for (int j = i+1; j < species_code.size(); j++) {
				if (species_prob.get(i).doubleValue() <
						species_prob.get(j).doubleValue()) {
					Collections.swap(species_prob, i, j);
					Collections.swap(species_code, i, j);
				}
			}
		}
		
		if (time_measure)
			System.out.println("Step 8: " + (System.currentTimeMillis()-initial_time)/1000);

		
		/*
		 * final: 輸出結果
		 */
		for (int i = 0; i < species_prob.size(); i++) {
				System.out.println(species_code.get(i) + "," +
						String.format("%.6f", species_prob.get(i)));
		}

		System.out.print("");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if (args.length == 1) {
			new VORESY(args[0], args[0] + ".dcparam");
		} else if (args.length == 2) {
			new VORESY(args[0], args[1]);
		} else {
			System.out.println("Usage: VORESY <sourcefile> (<dctreefile>)");
			if (RuntimeCFG.DEBUG == true)
				new VORESY(RuntimeCFG.WORKDIR + "TEMP/1314697011.wav",
						RuntimeCFG.WORKDIR + "TEMP/1314697011.wav.dcparam");
		}

	}

	private boolean time_measure = false;

}

enum habitat {
	RIVER, WATERFALL, 
	DITCH_WATER, DITCH_DRY, DITCH_EDGE,
	STAG, STAG_SHORE, STAG_PLANT,
	TEMPWAT, TEMPWAT_PLANT,
	GROVE_ARBOR, GROVE_BUSH, GROVE_GND, GROVE_TREEHOLE,
	GRASS_SHORT, GRASS_HIGH,
	CULT_PADDY, CULT_DRY, CULT_OCHRD, CULT_BAMBOO, CULT_FARMWASTE, CULT_CLEAR
};

enum dcParam {
	//有參考依據的
	LOC, HABI, ATEMP, ALT, MONTH,
	//沒參考依據的
	WTEMP, HUMI
};

class species_mapping {
	public int index;
	public String code;
	public String species;
	
	public species_mapping(int i, String c, String s) {
		index = i;
		code = c;
		species = s;
	}
	
};
