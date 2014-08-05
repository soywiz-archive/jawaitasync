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
	public void testLoopIf() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("a0[0:1000]ba1[1000:500]ba2[1500:1000]ba3[2500:500]ba4[3000:1000]b", LoopIfExample.class.getTypeName(), "testAsync", false);
	}

	@org.junit.Test
	public void testStatic() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("hello!0[0:1000]world!1", StaticExample.class.getTypeName(), "test1Async", true);
	}

	@org.junit.Test
	public void testStatic2() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("hello!0[0:1000]world!1", StaticExample.class.getTypeName(), "test2Async", true);
	}

	@org.junit.Test
	public void testAccessPrivate() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("public[0:1000]private[1000:1000]changed", AccessPrivateExample.class.getTypeName(), "testAsync", false);
	}

	@org.junit.Test
	public void testThrowNoCatch() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("[0:1000][Exception:ThrowingException]", ThrowNoCatchExample.class.getTypeName(), "testAsync", false);
	}

	@org.junit.Test
	public void testLong() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("Started[0:1000]Result:100000000", LongExample.class.getTypeName(), "testAsync", false);
	}

	@org.junit.Test
	public void testTransformingAwait() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("[0:1000]result:-100", TransformingAwaitExample.class.getTypeName(), "testAsync", false);
	}

	//@org.junit.Test
	//public void testTryCatch() throws Exception {
	//	TestAsyncClass.assertCallAsyncMethod("--", TryCatchExample.class.getTypeName(), "testAsync", false);
	//}

	/*
	@org.junit.Test
	public void testTryCatch2() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("--", TryCatchExample2.class.getTypeName(), "testAsync", false);
	}
	*/

	@org.junit.Test
	public void testCompositionWithArguments() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("{1}[0:1000]{2}{3:hello world}[1000:1000]", CompositionWithArgumentsExample.class.getTypeName(), "testAsync", false);
	}

	/*
	@org.junit.Test
	public void testAccessPrivateMethod() throws Exception {
		TestAsyncClass.assertCallAsyncMethod("public[0:1000]privateMethod", AccessPrivateMethodExample.class.getTypeName(), "testAsync", false);
	}
	*/
}
