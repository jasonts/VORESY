package voresy;
import java.io.*;
import java.util.zip.*;

public class unzip {
   final int BUFFER = 2048;
   
   public unzip(String filename) {
      try {
         BufferedOutputStream dest = null;
         FileInputStream fis = new FileInputStream(filename);
         ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
         ZipEntry entry;
         while((entry = zis.getNextEntry()) != null) {
            System.out.println("Extracting: " +entry);
            int count;
            byte data[] = new byte[BUFFER];
            // write the files to the disk
            FileOutputStream fos = new FileOutputStream(entry.getName());
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = zis.read(data, 0, BUFFER)) != -1) {
               dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
         }
         zis.close();
      } catch(Exception e) {
         e.printStackTrace();
      }
	   
   }

}