package jawaitasync;

import jawaitasync.loop.EventLoopHolder;
import jawaitasync.loop.MockedEventLoop;
import jawaitasync.processor.AsmProcessorLoader;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

public class SimpleTest {
	@org.junit.Test
	public void testName() throws Exception {
		EventLoopHolder.instance = new MockedEventLoop();

		//SVfs vfs = new FileSVfs(System.getProperty("user.dir") + "/../out");
		ClassLoader loader = new AsmProcessorLoader(ClassLoader.getSystemClassLoader());
		//Class clazz = loader.loadClass("PromiseExample");
		//Class clazz = loader.loadClass(PromiseExample.class.getName());
		Class clazz = loader.loadClass("PromiseExample");
		Method method = clazz.getMethod("testAsync");
		method.setAccessible(true);
		Object instance = clazz.newInstance();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream oldOut = System.out;
		System.setOut(new PrintStream(baos));

		Promise promise = (Promise) method.invoke(instance);
		EventLoopHolder.instance.loop();

		System.setOut(oldOut);

		Assert.assertEquals("hello!0[0:1000]world!1", baos.toString());
	}
}
