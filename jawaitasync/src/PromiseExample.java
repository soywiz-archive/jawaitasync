import jawaitasync.Promise;
import jawaitasync.PromiseTools;
import jawaitasync.loop.EventLoopHolder;

import static jawaitasync.Promise.await;

public class PromiseExample {
	public Promise testAsync() {
		//for (int n = 0; n < 2; n++) {
		int n = 0;
		System.out.print("hello!" + n++);
		await(PromiseTools.sleep(1000));
		System.out.print("world!" + n++);
		//}
		return Promise.complete(null);
		/*
		int n = 0;
        System.out.println("hello!" + n++);
        await(PromiseTools.sleep(1000));
        System.out.println("world!" + n++);
        await(PromiseTools.sleep(1000));
        System.out.println("end!" + n++);
        return Promise.complete(null);
        */
	}

	static {
		//System.out.println("PromiseExample.static");
	}

	public static void main(String[] args) throws Exception {
		new PromiseExample().testAsync();
		EventLoopHolder.instance.loop();
	}
}
