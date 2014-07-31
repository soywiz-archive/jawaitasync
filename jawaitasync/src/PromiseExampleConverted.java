import jawaitasync.EventLoop;
import jawaitasync.Promise;
import jawaitasync.PromiseRunnable;
import jawaitasync.PromiseTools;

import static jawaitasync.Promise.await;

public class PromiseExampleConverted {
	class TestAsyncState {
		int state = 0;
	}

    public Promise testAsync() {
		final PromiseRunnable<TestAsyncState>[] r = new PromiseRunnable[1];
		final Promise p = new Promise();
		final TestAsyncState state = new TestAsyncState();

		r[0] = (aaa) -> {
			switch (state.state) {
				case 0:
					System.out.println("hello!");
					state.state = 1;
					PromiseTools.sleep(1000).then(r[0]);
					return;
				case 1:
					System.out.println("world!");
					state.state = 2;
					PromiseTools.sleep(1000).then(r[0]);
					return;
				case 2:
					System.out.println("end!");
					p.resolve(null);
					return;
			}
		};

		r[0].run(null);

        return p;
    }

	public static void main(String[] args) throws Exception {
		PromiseExampleConverted base = new PromiseExampleConverted();
		base.testAsync().then(e -> {
		});
		EventLoop.loop();
	}
}
