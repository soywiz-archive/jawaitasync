package samples;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;

import static jawaitasync.Promise.await;

public class StaticExample {
	static public Promise test1Async() {
		int n = 0;
		System.out.print("hello!" + n++);
		await(PromiseTools.sleepAsync(1000));
		System.out.print("world!" + n++);
		return Promise.complete(null);
	}

	static public void test2Async() {
		int n = 0;
		System.out.print("hello!" + n++);
		await(PromiseTools.sleepAsync(1000));
		System.out.print("world!" + n++);
	}
}
