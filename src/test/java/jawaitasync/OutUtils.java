package jawaitasync;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class OutUtils {
	static String captureOutput(Runnable runnable) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream oldOut = System.out;
		try {
			System.setOut(new PrintStream(baos));
			runnable.run();
		} finally {
			System.setOut(oldOut);
		}
		return baos.toString();
	}
}
