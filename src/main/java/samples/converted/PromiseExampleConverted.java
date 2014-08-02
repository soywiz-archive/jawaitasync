package samples.converted;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;
import jawaitasync.ResultRunnable;
import jawaitasync.loop.EventLoopHolder;

public class PromiseExampleConverted {
	public Promise testAsync() {
		PromiseExampleConverted_testAsync_Runnable local = new PromiseExampleConverted_testAsync_Runnable(this);
		local.run(null);
		return local.promise;
	}

	static {
		System.out.println("samples.converted.PromiseExampleConverted.static");
	}

	public static void main(String[] args) throws Exception {
		new PromiseExampleConverted().testAsync();
		EventLoopHolder.instance.loop();
	}
}
