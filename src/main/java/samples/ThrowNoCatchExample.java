package samples;

import jawaitasync.PromiseTools;

import static jawaitasync.Promise.await;

public class ThrowNoCatchExample {
	public void testAsync() {
		await(PromiseTools.sleepAndThrowAsync(1000, new Exception("ThrowingException")));
	}
}
