package jawaitasync;

import samples.AwaitAsyncCompositionExample;
import samples.LoopExample;
import samples.PromiseExample;

public class SimpleTest {
	@org.junit.Test
	public void testSimple() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("hello!0[0:1000]world!1", PromiseExample.class.getTypeName(), "testAsync");
	}

	@org.junit.Test
	public void testComposition() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("{1}[0:1000]{2}{3}[1000:1000]", AwaitAsyncCompositionExample.class.getTypeName(), "testAsync");
	}

	/*
	@org.junit.Test
	public void testLoop() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("", LoopExample.class.getTypeName(), "testAsync");
	}
	*/
}
