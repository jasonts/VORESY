package voresy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class WEKAClassify {
	
	public WEKAClassify(String trainingData, String testData) {
		trainFilename = trainingData;
		testFilename = testData;		
	}
	
	/**
	 * 開始訓練樣本
	 * @return true: 訓練成功，false: 訓練失敗
	 */
	public boolean startTraining() {
		
		//using different thread to different classifier
		try {
			//import train dataset
			source = new DataSource(trainFilename);
			trainData = source.getDataSet();
			if (trainData.classIndex() == -1)
				trainData.setClassIndex(trainData.numAttributes() -1);
			
			//new a thread to kNN training
			if (enablekNN) {
				kc = new kNNClassify(trainData, this.testData);
				kNNThread = new Thread(kc);
				kNNThread.run();
			}
			
			//FLR training
			flr = new weka.classifiers.misc.FLR();
			flr.setRhoa(0.75);
//			flr.setOptions(weka.core.Utils.splitOptions("-R 0.75 -Y"));
			flr.buildClassifier(trainData);
		} catch (Exception e) {
			return false;
		}

		return true;
	}
	
	public boolean startTesting() {
		
		try {
			//import test dataset
			this.testData = new Instances(new BufferedReader(new FileReader(testFilename)));
			this.testData.setClassIndex(this.testData.numAttributes()-1);
			
			//classify and save to arraylist
			classifyed = new ArrayList<Double>();
			for (int i = 0; i < this.testData.size(); i++)
				classifyed.add(new Double(flr.classifyInstance(this.testData.get(i))));
			
			//merge the kNN's classify result
			if (enablekNN) {
				while (kNNThread.isAlive() == true)
					Thread.sleep(60);
				classifyed.addAll(kc.getClassifyResult());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}
		
		return true;
	}
	
	public void outputResult(String filename) {
		for (int i = 0; i < classifyed.size(); i++)
			System.out.println(classifyed.get(i));
	}
	
	public ArrayList<Double> getClassifyResult() {
		return classifyed;
	}
	
	public static void main(String args[]) {
		new WEKAClassify("/home/jason/文件/多多及林務局的nightmare/system_test/MFCC22.arff"
				, "/home/jason/文件/多多及林務局的nightmare/system_test/test.arff");
	}
	
	private DataSource source;
	private Instances trainData, testData;
	private ArrayList<Double> classifyed;
	private String trainFilename, testFilename;
	private weka.classifiers.misc.FLR flr;
	
	private kNNClassify kc = null;
	private Thread kNNThread;
	public boolean enablekNN = false;
}

class kNNClassify implements Runnable {

	public kNNClassify(Instances trainData, Instances testData) {
		this.trainData = trainData;
		this.testData = testData;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			// new a classifier and build the training data
			weka.classifiers.lazy.IBk kNN = new weka.classifiers.lazy.IBk();
			kNN.setOptions(weka.core.Utils.splitOptions(kNNOption));
			kNN.buildClassifier(trainData);

			//classify and save to arraylist
			classifyed = new ArrayList<Double>();
			for (int i = 0; i < testData.size(); i++)
				classifyed.add(new Double(kNN.classifyInstance(testData.get(i))));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public ArrayList<Double> getClassifyResult() {
		return classifyed;
	}
	
	private Instances trainData, testData;
	private ArrayList<Double> classifyed;
	private String kNNOption = "-K 5 -W 0 " +
			"-A \"weka.core.neighboursearch.LinearNNSearch " +
			"-A \\\"weka.core.EuclideanDistance -R first-last\\\"\" ";
	
}