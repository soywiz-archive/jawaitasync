import jawaitasync.loop.EventLoopHolder;
import jawaitasync.processor.AsmProcessor;
import jawaitasync.vfs.FileSVfs;
import jawaitasync.vfs.SVfs;
import jawaitasync.vfs.SVfsFile;
import samples.DownloadUrlExample;

public class ExampleMain {
	public static void main(String[] args) throws Exception {
		SVfs vfs = new FileSVfs(System.getProperty("user.dir") + "/target/classes");
		//new AsmProcessor().processFile(new SVfsFile(vfs, "samples/PromiseExample.class"));
		//new AsmProcessor().processFile(new SVfsFile(vfs, "samples/LoopExample.class"));
		//new AsmProcessor().processFile(new SVfsFile(vfs, "samples/CompositionExample.class"));
		new AsmProcessor().processFile(new SVfsFile(vfs, "samples/DownloadUrlExample.class"));

		//new CompositionExample().testAsync();
		new DownloadUrlExample().downloadFilesAsync();
		EventLoopHolder.instance.loop();
	}
}
