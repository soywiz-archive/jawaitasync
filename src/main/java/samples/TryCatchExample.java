package samples;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;

public class TryCatchExample {
	public Promise testAsync() {
		try {
			System.out.println("Started");
			await(PromiseTools.sleepAndThrowAsync(1000, new Exception("AfterASecondException")));
			System.out.println("NotRun");
		} catch (Exception exception) {
			System.out.println("Catch:" + exception.getMessage());
		}
		return complete(null);
	}
}
