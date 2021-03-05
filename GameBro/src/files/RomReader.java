package files;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RomReader {

	public static int[] readRom(String file) {
		
		byte[] bytes = null;
		
		try {
			InputStream stream = new FileInputStream(file);
			
			bytes = stream.readAllBytes();
			
		} catch (IOException e) {
			System.out.println("No file found at: " + file);
		}
		
		int[] out = new int[bytes.length];
		
		for(int i = 0; i < bytes.length; i++) {
			out[i] = bytes[i] & 0xFF;
		}
		
		return out;
	}
	
}
