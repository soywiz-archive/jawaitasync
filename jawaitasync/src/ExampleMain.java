import jawaitasync.EventLoop;
import jawaitasync.processor.AsmProcessor;

public class ExampleMain {
	public static void main(String[] args) throws Exception {
		new AsmProcessor().test();
		new PromiseExample().testAsync();
		EventLoop.loop();
	}
}
