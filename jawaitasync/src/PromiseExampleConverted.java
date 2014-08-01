import jawaitasync.EventLoop;
import jawaitasync.Promise;
import jawaitasync.PromiseTools;
import jawaitasync.ResultRunnable;

import static jawaitasync.Promise.await;

public class PromiseExampleConverted {
	public Promise testAsync() {
		PromiseExampleConverted_testAsync_Runnable local = new PromiseExampleConverted_testAsync_Runnable(this);
		local.run(null);
		return local.promise;
	}

	static {
		System.out.println("PromiseExampleConverted.static");
	}

	public static void main(String[] args) throws Exception {
		new PromiseExampleConverted().testAsync();
		EventLoop.loop();
	}
}

class PromiseExampleConverted_testAsync_Runnable implements ResultRunnable<Object> {
	public int state = 0;
	public Promise promise = new Promise();
	public PromiseExampleConverted local_this;

	public PromiseExampleConverted_testAsync_Runnable(PromiseExampleConverted paramPromiseExampleConverted)
	{
		this.local_this = paramPromiseExampleConverted;
	}

	public void run(Object paramObject) {
		switch (this.state) {
			case 0:
			default:
				System.out.println("hello!");
				PromiseTools.sleep(1000).then(this);this.state = 1;return;
			case 1:
				System.out.println("world!");
				promise.resolve(null);
		}
	}
}
