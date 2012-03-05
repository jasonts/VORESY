package voresy;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVWriter {

	public CSVWriter(String file) {

			try {
				fw = new FileWriter(new File(file));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
	
	public void write(double data[][]) throws IOException {
		int x_axis = data.length;
		int y_axis = data[0].length;
//		DecimalFormat format = new DecimalFormat( ".#####");
		
		for (int i = 0; i < x_axis; i++) {
			for (int j = 0; j < y_axis; j++) {
					//String str = format.format(Double.toString(data[i][j])); 
					fw.write(Double.toString(data[i][j]) + ",");
			}
			System.out.println("printed " + i + " of " + x_axis + "(" + ((double)i/x_axis) + ")");
			fw.write("\r\n");
		}
		
		fw.flush();
	}
	
	private FileWriter fw;
}
