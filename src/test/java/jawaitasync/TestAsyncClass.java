package jawaitasync;

import jawaitasync.loop.EventLoopHolder;
import jawaitasync.loop.MockedEventLoop;
import jawaitasync.processor.AwaitProcessorClassLoader;
import org.junit.Assert;

public class TestAsyncClass {
	static public void assertCallAsyncMethod(String expectedOutput, String className, String methodName, boolean asStatic) throws Exception {
		EventLoopHolder.instance = new MockedEventLoop();

		ClassLoader loader = new AwaitProcessorClassLoader(ClassLoader.getSystemClassLoader());
		Class clazz = loader.loadClass(className);

		Assert.assertEquals(expectedOutput, OutUtils.captureOutput(() -> {
			ClassLoader loader2 = loader;
			try {
				Promise promise = (Promise) clazz.getMethod(methodName).invoke(asStatic ? null : clazz.newInstance());
				EventLoopHolder.instance.loop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
	}

	static public void assertCallNoOutputAsync(String className, String methodName, boolean asStatic) throws Exception {
		EventLoopHolder.instance = new MockedEventLoop();

		ClassLoader loader = new AwaitProcessorClassLoader(ClassLoader.getSystemClassLoader());
		Class clazz = loader.loadClass(className);
		Promise promise = (Promise) clazz.getMethod(methodName).invoke(asStatic ? null : clazz.newInstance());
		EventLoopHolder.instance.loop();
	}
}
