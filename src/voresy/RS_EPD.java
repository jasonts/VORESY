package voresy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

/*
 * RS_EPD: RS End-Point Detection
 * 
 * changelog:
 * 20110531_1: 在能量曲線上，結合以前最大點切割其中的前後不處理的特性，可能可以改良錄音雜音
 * 20110610_1: 要給多多交論文的差，新增RS端點偵測中能量曲線的輸出csv功能
 * 20110614_1: 新增step5，最大點等片段切割
 * 20110616_1: 新增NORS模式
 * 20110618_1: 改變能量曲線的參數，20%->60%
 * 20110618_2: 等片段切割 (step5) deprecated.
 * 20110619_1: 新增輸出csv的觸發(RuntimeCFG.RS_CSVLOG)
 * 20110620_1: 斯文豪氏赤蛙的聲音太短了，測試70ms---已證實沒用
 * 20110718_1: 為了有些時候只要輸出圖表不輸出聲音，新增final step的觸發(RuntimeCFG.RS_EXPORTWAV)
 * 20110809_1: 修正能量曲線threshold的抓取方式
 * 20110814_1: RS端點偵測有時會切出<0秒的詭異片段，要過濾這種片段，所以不能拿掉那段code
 * 
 */
public class RS_EPD {
	
	public RS_EPD(String processFile) {
		
		sampleFile = new AudioFile(new File(processFile));
		sampleFile.init();
		sampleSignals = new Vector<signalclip>();
		
		/*
		 * Step 1: 能量曲線
		 * Thd(E) = 60% * MAX(E(n)) + AVG(E(n))
		 */
		
		/* 
		 * 20110809_1: 修正能量曲線threshold的抓取方式
		 */
		//100ms為單位抓取10段雜訊的threshold
		//if the file too short, maybe out of bounds exception
		double noiseThreshold = 1;
		
		/*
		 * 閥值偵測v1.1
		 */
/*
		try {
			for (int i = 0; i < 10; i++) {
				double newNoiseThreshold = 
					0.4 * sampleFile.getDuringSamplesMaxAmplitude((int)(RuntimeCFG.ms*100*i), (int)(RuntimeCFG.ms*100)) +
					1.0 * sampleFile.getDuringSamplesMeanAmplitude((int)(RuntimeCFG.ms*100*i), (int)(RuntimeCFG.ms*100));
				if (newNoiseThreshold < noiseThreshold) noiseThreshold = newNoiseThreshold;
			}
		} catch (Exception e) {}
		//System.out.println("RS thrd: " + String.format("%6f", noiseThreshold) + " ");
*/
		/*
		 * 閥值偵測v2.1
		 */
		noiseThreshold = 1;
		final double max_coeff = 0.4;
		//max_coeff = 0.15;
		if (sampleFile.getDurationSeconds() < 7) {
			//如果小於7秒
			//把整個檔案100ms切割，找出最小閥值片段當作閥值
			try {
				for (int i = 0; i < sampleFile.getFrameLength(); i+=RuntimeCFG.ms*100) {
					double newNT = 
						max_coeff * sampleFile.getDuringSamplesMaxAmplitude(i, (int)(RuntimeCFG.ms*100)) +
						1.0 * sampleFile.getDuringSamplesMeanAmplitude(i, (int)(RuntimeCFG.ms*100));
					if (newNT < noiseThreshold) noiseThreshold = newNT;
				}
			} catch (Exception e) {}
		} else {
			//大於7秒
			//抓1/3和2/3的位置，各取3秒(小於9秒會自動抓結尾)，再取平均
			int onethird = (int)sampleFile.getFrameLength() / 3;
			double onethirdNT = 1;
			for (int i = onethird; i < onethird + RuntimeCFG.sec*3; i+=RuntimeCFG.ms*100) {
				double newNT =
					max_coeff * sampleFile.getDuringSamplesMaxAmplitude(i, (int)(RuntimeCFG.ms*100)) +
					1.0 * sampleFile.getDuringSamplesMeanAmplitude(i, (int)(RuntimeCFG.ms*100));
				if (newNT < onethirdNT) onethirdNT = newNT;					
			}
			int twothird = onethird * 2;
			double twothirdNT = 1;
			for (int i = twothird; i < twothird + RuntimeCFG.sec*3; i+=RuntimeCFG.ms*100) {
				double newNT =
					max_coeff * sampleFile.getDuringSamplesMaxAmplitude(i, (int)(RuntimeCFG.ms*100)) +
					1.0 * sampleFile.getDuringSamplesMeanAmplitude(i, (int)(RuntimeCFG.ms*100));
				if (newNT < twothirdNT) twothirdNT = newNT;
			}
			noiseThreshold = (onethirdNT + twothirdNT) / 2. ;
		}
		//System.out.println("after: " + String.format("%6f", noiseThreshold) + " ");

//20110809_1
		//50ms作為一個單位，抓取能量曲線
		//20110531_1: 前後100ms不去抓取
		int energyBound = (int)(RuntimeCFG.ms*50);
		int ignoreHeadTail;
		if (RuntimeCFG.CUTEDGE)
			ignoreHeadTail = (int) (RuntimeCFG.ms*100);
		else
			ignoreHeadTail = 0;
		
		for (int i = ignoreHeadTail; i < sampleFile.getFrameLength()-ignoreHeadTail; i += energyBound) {
			//抓到頭
			if (sampleFile.getDuringSamplesMeanAmplitude(i, energyBound) > noiseThreshold) {
				//抓尾巴
				for (int j = i+energyBound; j < sampleFile.getFrameLength()-ignoreHeadTail; j += energyBound) {

					if (sampleFile.getDuringSamplesMeanAmplitude(j, energyBound) < noiseThreshold) {
						sampleSignals.add(new signalclip(i, 
								(j+energyBound-1)> sampleFile.getFrameLength() ? (int)(sampleFile.getFrameLength()-1) : (j+energyBound-1)  ));
						i = j+energyBound;
						break;
					}
				}
			} else continue;
		}
		
		//20110610_1
		/* 能量曲線csv輸出
		 * format: AMP, THRE, SEG
		 * 
		 * 20110619_1 觸發判斷 RS_CSVLOG
		 */
		if (RuntimeCFG.RS_CSVLOG_1) {
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter(processFile + ".energy.csv"));
				// create array of segment
				boolean segment[] = new boolean[(int) sampleFile.getFrameLength()];
				for (int i = 0; i < sampleSignals.size(); i++) {
					int head = sampleSignals.get(i).getStartPosition();
					int tail = sampleSignals.get(i).getEndPosition();
					for (int j = head; j <= tail; j++) 
						segment[j] = true;
				}
				
				for (int i = 0; i < sampleFile.getFrameLength(); i++) {
	
					bw.write(i + "," + Math.abs(sampleFile.getSample(i)) + "," +
								noiseThreshold + "," +
								(segment[i] ? 2 : 0) +
								"\r\n");
				}
				bw.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		/*
		 * Step 2: 過零率，以200ms作為掃描區間
		 */
		//System.out.println("clips quantity before ZCR: " + sampleSignals.size());
		if (!RuntimeCFG.NORS) {
			if (!RuntimeCFG.noZCR) {
				int ZCRScanBound = (int)(RuntimeCFG.ms*200);
				
				sampleFile.startZCR();
				for (int i = 0; i < sampleSignals.size(); i++) {
					int ZCRBounds[] = null;
					int ZCRThreshold = sampleFile.getZCRThreshold();
					int newPosition;
					
					//取得原本的上下界
					int upperBound = sampleSignals.get(i).getEndPosition();
					int lowerBound = sampleSignals.get(i).getStartPosition();
					
					//取得左界ZCR數值
					ZCRBounds = sampleFile.getDuringSamplesZCR(lowerBound, ZCRScanBound, false);
					//往左延伸
					newPosition = -1;
					for (int j = 0; j < ZCRBounds.length; j++) {
						if (ZCRThreshold > ZCRBounds[ZCRBounds.length-j-1]) {
							newPosition = j;
							break;
						}
					}
					newPosition += lowerBound;
					if (newPosition != -1) {
						sampleSignals.get(i).setStartPosition(newPosition);
					}
					
					//取得右界ZCR數值
					ZCRBounds = sampleFile.getDuringSamplesZCR(upperBound, ZCRScanBound, true);
					//往右延伸
					newPosition = -1;
					for (int j = 0; j < ZCRBounds.length; j++) {
						if (ZCRThreshold > ZCRBounds[j]) {
							newPosition = j;
							break;
						}
					}
					newPosition += upperBound;
					if (newPosition != -1) {
						sampleSignals.get(i).setEndPosition(newPosition);
					}
					
				}
		
				//如果延伸之後有重複到，把那幾個聲音樣本合併為單一聲音樣本
				for (int i = 0; i < sampleSignals.size(); i++) {
					//maybe out of index
					try {
						if (sampleSignals.get(i).getEndPosition() >= sampleSignals.get(i+1).getStartPosition()) { 
							sampleSignals.get(i).setEndPosition(sampleSignals.get(i+1).getEndPosition());
							sampleSignals.remove(i+1);
							i--;
						}
					} catch (Exception e) {}
				}
				
				/*
				 * 20110612: output the original ZCRed value
				 * 20110619: 觸發判斷 RS_CSVLOG
				 */
				if (RuntimeCFG.RS_CSVLOG_2) {
					String dumpCSVFilename = processFile + ".RS.ZCR_nofilter.csv";
					sampleFile.dumpAudioCSVWithSelection(dumpCSVFilename, sampleSignals, noiseThreshold);
				}
			}
			/*
			 * Step 3:
			 * 如果切割後的聲音長度是否小於0.2秒，把那段移除掉
			 * 20110620_1: 斯文豪氏赤蛙的聲音太短了，測試70ms
			 * 20110814_1: RS端點偵測有時會切出<0秒的詭異片段，要過濾這種片段，所以不能拿掉這段code
			 */
			for (int i = 0; i < sampleSignals.size(); i++) {
				try {	//maybe had out of range exception
					if (sampleSignals.get(i).getFrameLength() <= RuntimeCFG.ms*RuntimeCFG.minimalSegmentLen) 
						sampleSignals.remove(i--);
				} catch (Exception e) {}
			}
			sampleSignals.trimToSize();
			//System.out.println("clips quantity after 0.2sec drop: " + sampleSignals.size());
			
		}

		/*
		 * Step 4: 輸出
		 * csv: 輸出RS的分段、每段的長度、每段的響度
		 * 最後輸出RS的每一段的音訊
		 * 
		 * 20110619_1: 觸發判斷 RS_CSVLOG
		 */
		if (RuntimeCFG.RS_CSVLOG_3) {
			String dumpCSVFilename2 = processFile + ".RS.ZCR_filtered.csv";
			sampleFile.dumpAudioCSVWithSelection(dumpCSVFilename2, sampleSignals, noiseThreshold);
		}
		
		/*
		 * Step 5: 最大點等片段切割(控制於RuntimeCFG.MAXSEGMENT)
		 * Work Progressing
		 * forward and reward 0.5sec
		 * 
		 * 20110618_2 等片段切割deprecated.
		 */
		/*
		if (RuntimeCFG.MAXSEGMENT && !RuntimeCFG.NORS) {
			for (int i = 0; i < sampleSignals.size(); i++) {
				//如果 整個片段<1sec 不處理
				if (sampleSignals.get(i).getFrameLength() <= RuntimeCFG.sec)
					continue;
				//抓取最大的聲音點位置
				int start = sampleSignals.get(i).getStartPosition();
				int end = sampleSignals.get(i).getEndPosition();
				int maxAmpPos = sampleFile.getDuringSamplesMaxAmplitudePosition(start, end);
				//已經過濾了，所以不可能出現往前往後都超過的狀況
				if (maxAmpPos - 500*RuntimeCFG.ms < start) {
					//如果往前會超過，就把多出來的片段長度加到後面去
					end = maxAmpPos + (int)RuntimeCFG.ms*500 + (int)(RuntimeCFG.ms*500 - (maxAmpPos-start));
				} else if (maxAmpPos + 500*RuntimeCFG.ms > end) {
					//如果往後會超過，就加到前面去
					start = maxAmpPos - (int)RuntimeCFG.ms*500 - (int)(RuntimeCFG.ms*500 + maxAmpPos - end);
				} else {
					//正常狀況
					end = (int) (maxAmpPos + RuntimeCFG.ms*500);
					start = (int) (maxAmpPos - RuntimeCFG.ms*500);
				}
				sampleSignals.get(i).setEndPosition(end);
				sampleSignals.get(i).setStartPosition(start);
				System.out.println("segment len: " + (end-start) + "\t" + end + "\t" + start);
				
			}
		}
		*/
		
		/*
		 * Last step: 把這些片段輸出成wav, and add to RS fileList
		 */
		if (RuntimeCFG.RS_EXPORTWAV) {
			fileList = new ArrayList<String>();
			//sampleFile.saveAudioClips(sampleSignals, processFile + ".RS.>InDeX<.wav");
			for (int i = 0; i < sampleSignals.size(); i++) {
				String str = processFile + ".RS." + String.format("%02d", i) + ".wav";
				sampleFile.saveAudioClip(sampleSignals.get(i), str);
				fileList.add(str);
			}
			
		}
		
	}
	
	/**
	 * get the RS filelist
	 * @return 
	 */
	public ArrayList<?> getFileList() {
		return fileList;
	}
	
	public Vector<signalclip> getSignalClips() {
		return sampleSignals;
	}

	AudioFile sampleFile;
	Vector<signalclip> sampleSignals;
	ArrayList<String> fileList;
	
}

class signalclip {
	
	public signalclip(int spos, int epos) {
		start = spos;
		end = epos;
	}
	public void setStartPosition(int pos) {start = pos; }
	public void setEndPosition(int pos) { end = pos; }
	public int getStartPosition() { return start; }
	public int getEndPosition() { return end; }
	public int getFrameLength() { return end-start+1; }
	private int start, end;
}
