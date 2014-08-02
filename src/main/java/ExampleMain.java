import jawaitasync.PromiseTools;
import jawaitasync.loop.EventLoopHolder;
import jawaitasync.processor.AsmProcessor;
import jawaitasync.processor.AsmProcessorLoader;
import jawaitasync.vfs.FileSVfs;
import jawaitasync.vfs.SVfs;
import jawaitasync.vfs.SVfsFile;
import samples.DownloadUrlExample;

import java.io.IOException;

import static jawaitasync.Promise.await;

public class ExampleMain {
	public static void main(String[] args) throws Exception {
		ClassLoader loader = new AsmProcessorLoader(ClassLoader.getSystemClassLoader());
		Class start = loader.loadClass(Start.class.getName());
		start.getMethod("start").invoke(null);
		//SVfs vfs = new FileSVfs(System.getProperty("user.dir") + "/target/classes");
		//new AsmProcessor().processFile(new SVfsFile(vfs, "samples/PromiseExample.class"));
		//new AsmProcessor().processFile(new SVfsFile(vfs, "samples/LoopExample.class"));
		//new AsmProcessor().processFile(new SVfsFile(vfs, "samples/CompositionExample.class"));
		//new AsmProcessor().processFile(new SVfsFile(vfs, "samples/DownloadUrlExample.class"));

		EventLoopHolder.instance.loop();
	}
}

