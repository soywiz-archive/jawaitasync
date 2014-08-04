package samples;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;

public class LongExample {
	public void testAsync() {
		System.out.print("Started");
		long result = await(PromiseTools.sleepAsync(1000, 100000000L));
		System.out.print("Result:" + result);
	}
}
