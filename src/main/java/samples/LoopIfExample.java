package samples;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;

public class LoopIfExample {
	public Promise testAsync() {
		for (int n = 0; n < 5; n++) {
			System.out.print("a" + n);
			if ((n % 2) == 0) {
				await(PromiseTools.sleepAsync(1000));
			} else {
				await(PromiseTools.sleepAsync(500));
			}
			System.out.print("b");
		}
		return complete(null);
	}
}
