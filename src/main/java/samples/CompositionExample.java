package samples;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;
import jawaitasync.loop.EventLoopHolder;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;
import static jawaitasync.PromiseTools.sleep;

public class CompositionExample {
	public Promise testAsync() {
		System.out.print("{1}");
		await(sleep(1000));
		System.out.print("{2}");
		await(test2Async());
		return complete(null);
	}

	public Promise test2Async() {
		System.out.print("{3}");
		await(sleep(1000));
		return complete(null);
	}
}
