package voresy;

public class RS_EPD_MT implements Runnable {

	public RS_EPD_MT(String file) {
		filename = file;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		new RS_EPD(filename);
	}
	private String filename;
	
}