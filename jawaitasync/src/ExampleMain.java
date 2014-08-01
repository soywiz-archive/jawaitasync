import jawaitasync.loop.EventLoop;
import jawaitasync.loop.EventLoopHolder;
import jawaitasync.processor.AsmProcessor;

public class ExampleMain {
	public static void main(String[] args) throws Exception {
		//System.out.println(ExampleMain.class.getResourceAsStream(ExampleMain.class.getName() + ".class"));
		new AsmProcessor().test();
		new PromiseExample().testAsync();
		EventLoopHolder.instance.loop();
	}
}
