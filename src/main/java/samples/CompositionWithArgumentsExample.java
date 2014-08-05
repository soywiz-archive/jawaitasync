package samples;

import jawaitasync.Promise;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;
import static jawaitasync.PromiseTools.sleepAsync;

public class CompositionWithArgumentsExample {
	public Promise testAsync() {
		System.out.print("{1}");
		await(sleepAsync(1000));
		System.out.print("{2}");
		await(test2Async("hello world"));
		return complete(null);
	}

	public Promise test2Async(String helloWorld) {
		System.out.print("{3:" + helloWorld + "}");
		await(sleepAsync(1000));
		return complete(null);
	}
}
