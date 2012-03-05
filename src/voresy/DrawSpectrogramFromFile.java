package voresy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DrawSpectrogramFromFile {

	public DrawSpectrogramFromFile(String Filename) {
		// TODO Auto-generated constructor stub
		try {
			reader = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(Filename + ".txt")));
			BMPFile = new FileOutputStream(new File(Filename + ".bmp"));
			
			String in;
			//��Ū���e���S�Ϊ��F��
			for (int i = 0; i < 17; i++)
				in = reader.readLine();
			
			//��header�g�X��
			BMPFile.write(BMP_header());
			
			//�o��}�l�ӯu��
			while((in = reader.readLine()) != null) {
				String spilted[] = in.split("\t");
				double dB[] = raw_to_dB(spilted);
				byte colormap[] = dB_mapping(dB);
				//�g�Jpixel���줸���
				BMPFile.write(colormap);
			}
			BMPFile.flush();
			
			reader.close();
			BMPFile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// reference: http://crazycat1130.pixnet.net/blog/post/1345538
	private byte[] BMP_header() {
		byte[] header = new byte[54];
		
		//Bitmap File Header
		//Identity: "BM", 2 bytes
		header[0] = 0x42;	//'B'
		header[1] = 0x4d;	//'M'
		//bmp_size: 4 bytes
		//54 + 51200 pixels * 3 = 153654 (0x00025386)
		header[2] = 0x36;
		header[3] = 0x58;
		header[4] = 0x02;
		header[5] = 0x00;
		//reserved: NULL, 4 bytes
		//bitmap data offset: 0x36, 4 bytes
		header[10] = 0x36;
		header[11] = 0x00;
		
		//Bitmap Info Header
		//Bitmap Header Size: 0x28, 4 bytes
		header[14] = 0x28;
		header[15] = 0x00;
		//Width: 100 pixels = 0x64, 4 bytes
		header[18] = 0x64;
		header[19] = 0x00;
		//Height: 512 pixels = 0x0200, 4 bytes
		header[22] = 0x00;
		header[23] = 0x02;
		//Planes: 1, 2 bytes
		header[26] = 0x01;
		header[27] = 0x00;
		//Bits per pixel: 24 = 0x18, 2 bytes
		header[28] = 0x18;
		header[29] = 0x00;
		//Compression: None = 0x00, 4 bytes
		//Bitmap Data Size: 51200 * 3 = 153600 = 0x00025800, 4 bytes
		header[34] = 0x00;
		header[35] = 0x58;
		header[36] = 0x02;
		header[37] = 0x00;
		//useless 16 bytes
		//H-Res, V-Res, Used Colors, Important Colors
		
		return header;
	}
	
	private double[] raw_to_dB(String raw[]) { 
		double result[] = new double[raw.length-1];
		for (int i = 0; i < result.length; i++) {
			result[i] = 10. * Math.log10(
					Math.abs(Double.parseDouble(raw[i+1])));
		}
		return result;
	}
	
	private byte[] dB_mapping(double dBs[]) {
		//RGB, 3B per pixel
		byte pixels_byte[] = new byte[dBs.length * 3];
		
		//0~-100dB: FFFFFF~000000
		// color(gray) = (255 / 100) * dB
		for (int i = 0; i < dBs.length; i++) {
			int color = (int) (2.55*(Math.abs(dBs[i])));
			//reverse the color
			color = ~color;
			pixels_byte[3*i] = (byte) color;
			pixels_byte[3*i+1] = (byte) color;
			pixels_byte[3*i+2] = (byte) color;
		}
		return pixels_byte;
	}

	private BufferedReader reader;
	private FileOutputStream BMPFile;
	
}
