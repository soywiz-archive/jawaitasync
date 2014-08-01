package samples;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;

public class LoopExample {
	public Promise testAsync() {
		for (int n = 0; n < 2; n++) {
			System.out.print("a");
			await(PromiseTools.sleep(1000));
			System.out.print("b");
		}
		return complete(null);
	}
}
