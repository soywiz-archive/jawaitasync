package samples.converted;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;
import jawaitasync.ResultRunnable;

public class PromiseExampleConverted_testAsync_Runnable implements ResultRunnable<Object> {
	public int state = 0;
	public Promise promise = new Promise();
	public PromiseExampleConverted local_this;

	public PromiseExampleConverted_testAsync_Runnable(PromiseExampleConverted paramPromiseExampleConverted) {
		this.local_this = paramPromiseExampleConverted;
	}

	public void run(Object paramObject) {
		switch (this.state) {
			case 0:
			default:
				System.out.print("hello!");
				PromiseTools.sleepAsync(1000).then(this);
				this.state = 1;
				return;
			case 1:
				System.out.print("world!");
				promise.resolve(null);
		}
	}
}
