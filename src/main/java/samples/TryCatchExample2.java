package samples;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;

public class TryCatchExample2 {
	public void testAsync() {
		try {
			await(PromiseTools.sleepAsync(1000));
		} catch (Exception exception) {
		}
	}
}
