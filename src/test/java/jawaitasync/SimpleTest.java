package jawaitasync;

import samples.*;

public class SimpleTest {
	@org.junit.Test
	public void testSimple() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("hello!0[0:1000]world!1", PromiseExample.class.getTypeName(), "testAsync", false);
	}

	@org.junit.Test
	public void testSimple2() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("hello!0[0:1000]world!1", Promise2Example.class.getTypeName(), "testAsync", false);
	}

	@org.junit.Test
	public void testComposition() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("{1}[0:1000]{2}{3}[1000:1000]", CompositionExample.class.getTypeName(), "testAsync", false);
	}

	@org.junit.Test
	public void testLoop() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("a[0:1000]ba[1000:1000]b", LoopExample.class.getTypeName(), "testAsync", false);
	}

	@org.junit.Test
	public void testStatic() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("hello!0[0:1000]world!1", StaticExample.class.getTypeName(), "test1Async", true);
	}

	@org.junit.Test
	public void testStatic2() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("hello!0[0:1000]world!1", StaticExample.class.getTypeName(), "test2Async", true);
	}

	//@org.junit.Test
	//public void testTryCatch() throws Exception {
	//	TestAsyncClass.assertCallAsyncMethod("--", TryCatchExample.class.getTypeName(), "testAsync", false);
	//}

	//@org.junit.Test
	//public void testTryCatch2() throws Exception {
	//	TestAsyncClass.assertCallAsyncMethod("--", TryCatchExample2.class.getTypeName(), "testAsync", false);
	//}

	@org.junit.Test
	public void testThrowNoCatch() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("[0:1000][Exception:ThrowingException]", ThrowNoCatchExample.class.getTypeName(), "testAsync", false);
	}

	//@org.junit.Test
	//public void testTransformingAwait() throws Exception {
	//	TestAsyncClass.assertCallAsyncMethod("--", TransformingAwaitExample.class.getTypeName(), "testAsync", false);
	//}
}
