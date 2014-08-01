import jawaitasync.EventLoop;
import jawaitasync.Promise;
import jawaitasync.PromiseTools;
import jawaitasync.processor.AsmProcessor;

import static jawaitasync.Promise.await;

public class PromiseExample {
    public Promise testAsync() {
		System.out.println("hello!");
		await(PromiseTools.sleep(1000));
		System.out.println("world!");
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
		System.out.println("PromiseExample.static");
	}

	public static void main(String[] args) throws Exception {
		new PromiseExample().testAsync();
		EventLoop.loop();
	}
}
