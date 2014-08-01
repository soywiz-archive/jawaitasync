package jawaitasync;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamUtils {
	static public byte[] load(InputStream is) {
		byte[] test = null;
		if (is != null) {
			try {
				test = new byte[is.available()];
				is.read(test);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return test;
	}
}
