package jawaitasync;

import samples.CompositionExample;
import samples.LoopExample;
import samples.PromiseExample;
import samples.TryCatchExample;

public class SimpleTest {
	@org.junit.Test
	public void testSimple() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("hello!0[0:1000]world!1", PromiseExample.class.getTypeName(), "testAsync");
	}

	@org.junit.Test
	public void testComposition() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("{1}[0:1000]{2}{3}[1000:1000]", CompositionExample.class.getTypeName(), "testAsync");
	}

	@org.junit.Test
	public void testLoop() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("a[0:1000]ba[1000:1000]b", LoopExample.class.getTypeName(), "testAsync");
	}

	//@org.junit.Test
	//public void testTryCatch() throws Exception {
	//	TestAsyncClass.assertCallAsyncMethod("--", TryCatchExample.class.getTypeName(), "testAsync");
	//}
}
