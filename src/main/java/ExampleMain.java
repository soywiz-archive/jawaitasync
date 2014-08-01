import jawaitasync.loop.EventLoopHolder;
import jawaitasync.processor.AsmProcessor;
import jawaitasync.vfs.FileSVfs;
import jawaitasync.vfs.SVfs;
import jawaitasync.vfs.SVfsFile;
import samples.LoopExample;
import samples.PromiseExample;

public class ExampleMain {
	public void test(SVfs vfs) throws Exception {
	}

	public void test() throws Exception {
		test();
	}

	public static void main(String[] args) throws Exception {
		SVfs vfs = new FileSVfs(System.getProperty("user.dir") + "/target/classes");
		//new AsmProcessor().processFile(new SVfsFile(vfs, "samples/PromiseExample.class"));
		new AsmProcessor().processFile(new SVfsFile(vfs, "samples/LoopExample.class"));

		new LoopExample().testAsync();
		EventLoopHolder.instance.loop();
	}
}
