package samples;

import jawaitasync.Promise;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;

public class SeveralArguments {
	static public Promise<byte[]> testAsync() {
		return test2Async("", (byte)'c');
	}

	static public Promise<byte[]> test2Async(String string, byte c) {
		return complete(await(Promise.resolved(null)));
	}
}
