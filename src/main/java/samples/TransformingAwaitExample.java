package samples;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;
import jawaitasync.loop.EventLoopHolder;

import static jawaitasync.Promise.await;

public class TransformingAwaitExample {
	public void testAsync() {
		int result = transform(await(PromiseTools.sleepAsync(1000, 100)));
		System.out.print("result:" + result);
	}

	public int transform(int a) {
		return -a;
	}
}
