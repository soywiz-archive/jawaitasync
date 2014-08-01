import jawaitasync.EventLoop;
import jawaitasync.processor.AsmProcessor;

public class ExampleMain {
	public static void main(String[] args) throws Exception {
		new AsmProcessor().test();
		PromiseExample pe = new PromiseExample();
		pe.testAsync();
		EventLoop.loop();
	}
}
