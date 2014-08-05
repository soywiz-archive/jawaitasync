package samples;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;

public class TryCatchFinallyExample {
	public Promise testAsync() {
		try {
			System.out.print("Started");
			await(PromiseTools.sleepAndThrowAsync(1000, new Exception("AfterASecondException")));
			System.out.print("NotRun");
		} catch (Exception exception) {
			System.out.print("MyCatch:" + exception.getMessage());
		} finally {
			System.out.print("MyFinally");
		}
		return complete(null);
	}
}
