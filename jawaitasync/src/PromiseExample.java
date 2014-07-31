import jawaitasync.Promise;
import jawaitasync.PromiseTools;

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
}
