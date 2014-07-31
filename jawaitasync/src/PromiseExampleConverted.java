import jawaitasync.EventLoop;
import jawaitasync.Promise;
import jawaitasync.ResultRunnable;
import jawaitasync.PromiseTools;

public class PromiseExampleConverted {
    public Promise testAsync() {
		final ResultRunnable[] r = new ResultRunnable[1];
		final Promise p = new Promise();

		r[0] = new ResultRunnable() {
			public int state = 0;
			public int local_n = 0;

			@Override
			public void run(Object aaa) {
				switch (this.state) {
					case 0:
						this.local_n = 0;
						System.out.println("hello!" + this.local_n++);
						this.state = 1;
						PromiseTools.sleep(1000).then(r[0]);
						return;
					case 1:
						System.out.println("world!" + this.local_n++);
						this.state = 2;
						PromiseTools.sleep(1000).then(r[0]);
						return;
					case 2:
						System.out.println("end!" + this.local_n++);
						p.resolve(null);
						return;
				}
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
