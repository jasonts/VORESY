package voresy;

public class RuntimeCFG {
	
	/**
	 * DEBUG FLAG = TRUE
	 */
	public static boolean DEBUG = true;
	
	/**
	 * THREADS = 2
	 */
	public static int THREADS = 2;
	
	/**
	 * WORKDIR = "/VORESY"
	 */
	//public static String WORKDIR = "C:/VORESY/";	//Windows
	public static String WORKDIR = "~/VORESY/";		//Linux
	
	/*********************************************
	 * DON'T MODIFY
	 */
	/**
	 * WORKTEMPDIR = "/VORESY/TEMP"
	 */
	public static String WORKTEMPDIR = WORKDIR + "TEMP/";
	
	/**
	 * EXTERNAL_PROGRAM_DIR = "/VORESY/INCLUDE/"
	 * linux: "/usr/bin/"
	 */
	//public static String EXTERNAL_PROGRAM_DIR = WORKDIR + "INCLUDE/";
	public static String EXTERNAL_PROGRAM_DIR = "/usr/bin/";
	
	/**
	 * sec = 44100
	 * ms = 44.1
	 */
	public static int sec = 44100;
	public static double ms = sec/1000.;
	
	/**
	 * MFCC Axis
	 * 13 22
	 */
	public static int mfcc_axis = 22;
	
	/**
	 * WEKA Training data
	 */
	public static String trainData = WORKDIR + "MFCC/MFCC22.arff";
	/**
	 * MAXSEGMENT: 觸發最大點切割?
	 */
	public static boolean MAXSEGMENT = false;
	
	/**
	 * NORS: NO RS edge detect?
	 */
	public static boolean NORS = false;
	
	/**
	 * noZCR: no zero crossing rating?
	 */
	public static boolean noZCR = false;
	
	/**
	 * RS_CSVLOG: 是否dump出
	 */
	public static boolean RS_CSVLOG_1 = false;
	public static boolean RS_CSVLOG_2 = false;
	public static boolean RS_CSVLOG_3 = false;
	public static boolean RS_EXPORTWAV = false;

	/**
	 * CUTEDGE: 是否要頭尾切割
	 */
	public static final boolean CUTEDGE = true;
	
	/**
	 * minimalSegmentLen: RS最短片段的長度
	 */
	public static double minimalSegmentLen = 200;

}
