import jawaitasync.PromiseTools;
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
		PromiseTools.runTaskAsync(() -> {
			long m = 0;
			for (long n = 0; n < 2000000000; n++) m += n;
			return m;
		}).then((result) -> {
			System.out.println(result);
		});

		new DownloadUrlExample().downloadFilesAsync();

		EventLoopHolder.instance.loop();
	}
}
