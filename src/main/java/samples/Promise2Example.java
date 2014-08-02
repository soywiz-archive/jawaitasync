package samples;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;
import jawaitasync.loop.EventLoopHolder;

import static jawaitasync.Promise.await;

public class Promise2Example {
	public void testAsync() {
		int n = 0;
		System.out.print("hello!" + n++);
		await(PromiseTools.sleepAsync(1000));
		System.out.print("world!" + n++);
	}
}
